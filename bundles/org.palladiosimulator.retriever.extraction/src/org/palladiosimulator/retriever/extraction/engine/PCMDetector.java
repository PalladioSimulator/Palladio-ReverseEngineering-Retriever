package org.palladiosimulator.retriever.extraction.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.palladiosimulator.retriever.extraction.commonalities.CompUnitOrName;
import org.palladiosimulator.retriever.extraction.commonalities.ComponentBuilder;
import org.palladiosimulator.retriever.extraction.commonalities.CompositeBuilder;
import org.palladiosimulator.retriever.extraction.commonalities.EntireInterface;
import org.palladiosimulator.retriever.extraction.commonalities.InterfaceName;
import org.palladiosimulator.retriever.extraction.commonalities.JavaInterfaceName;
import org.palladiosimulator.retriever.extraction.commonalities.JavaOperationName;
import org.palladiosimulator.retriever.extraction.commonalities.Operation;
import org.palladiosimulator.retriever.extraction.commonalities.OperationInterface;
import org.palladiosimulator.retriever.extraction.commonalities.OperationName;
import org.palladiosimulator.retriever.extraction.commonalities.PCMDetectionResult;
import org.palladiosimulator.retriever.extraction.commonalities.ProvisionsBuilder;
import org.palladiosimulator.retriever.extraction.commonalities.RequirementsBuilder;

/**
 * This class is used to detect and hold all relevant elements found during the processing of rules.
 * It provides methods to detect and retrieve PCM elements. After all rules are parsed, this class
 * holds the results as "simple" java objects not yet transformed to real PCM objects like PCM Basic
 * Components.
 */
public class PCMDetector {
    private static final Logger LOG = Logger.getLogger(PCMDetector.class);

    private final Map<CompUnitOrName, ComponentBuilder> components = new ConcurrentHashMap<>();
    private final Map<String, CompositeBuilder> composites = new ConcurrentHashMap<>();
    private final ProvisionsBuilder compositeProvisions = new ProvisionsBuilder();
    private final RequirementsBuilder compositeRequirements = new RequirementsBuilder();

    private static String getFullUnitName(final CompUnitOrName unit) {
        // TODO this is potentially problematic, maybe restructure
        // On the other hand, it is still fit as a unique identifier,
        // since types cannot be declared multiple times.

        final List<String> names = getFullUnitNames(unit);
        if (!names.isEmpty()) {
            return names.get(0);
        }
        return null;
    }

    private static List<String> getFullUnitNames(final CompUnitOrName unit) {
        if (!unit.isUnit()) {
            return List.of(unit.name());
        }

        final List<String> names = new ArrayList<>();
        for (final Object type : unit.compilationUnit()
            .get()
            .types()) {
            if (type instanceof AbstractTypeDeclaration) {
                names.add(getFullTypeName((AbstractTypeDeclaration) type));
            }
        }

        return names;
    }

    private static String getFullTypeName(final AbstractTypeDeclaration type) {
        return type.getName()
            .getFullyQualifiedName();
    }

    public void detectComponent(final CompUnitOrName unit) {
        if (!unit.isUnit()) {
            this.components.put(unit, new ComponentBuilder(unit));
            return;
        }
        for (final Object type : unit.compilationUnit()
            .get()
            .types()) {
            if (type instanceof TypeDeclaration) {
                this.components.put(unit, new ComponentBuilder(unit));
                final ITypeBinding binding = ((TypeDeclaration) type).resolveBinding();
                this.detectProvidedInterfaceWeakly(unit, binding);
            }
        }
    }

    public void detectRequiredInterface(final CompUnitOrName unit, final InterfaceName interfaceName) {
        this.detectRequiredInterface(unit, interfaceName, false);
    }

    public void detectRequiredInterface(final CompUnitOrName unit, final InterfaceName interfaceName,
            final boolean compositeRequired) {
        if (this.components.get(unit) == null) {
            this.components.put(unit, new ComponentBuilder(unit));
        }
        final EntireInterface iface = new EntireInterface(interfaceName);
        this.detectRequiredInterface(unit, compositeRequired, false, iface);
    }

    public void detectRequiredInterface(final CompUnitOrName unit, final FieldDeclaration field) {
        this.detectRequiredInterface(unit, field, false, false);
    }

    public void detectRequiredInterfaceWeakly(final CompUnitOrName unit, final FieldDeclaration field) {
        this.detectRequiredInterface(unit, field, false, true);
    }

    private void detectRequiredInterface(final CompUnitOrName unit, final FieldDeclaration field,
            final boolean compositeRequired, final boolean detectWeakly) {
        if (this.components.get(unit) == null) {
            this.components.put(unit, new ComponentBuilder(unit));
        }
        @SuppressWarnings("unchecked")
        final List<OperationInterface> ifaces = ((List<VariableDeclaration>) field.fragments()).stream()
            .map(x -> x.resolveBinding())
            .filter(x -> x != null)
            .map(x -> x.getType())
            .map(x -> new EntireInterface(x, new JavaInterfaceName(NameConverter.toPCMIdentifier(x))))
            .collect(Collectors.toList());
        this.detectRequiredInterface(unit, compositeRequired, detectWeakly, ifaces);
    }

    public void detectRequiredInterface(final CompUnitOrName unit, final SingleVariableDeclaration parameter) {
        this.detectRequiredInterface(unit, parameter, false);
    }

    private void detectRequiredInterface(final CompUnitOrName unit, final SingleVariableDeclaration parameter,
            final boolean compositeRequired) {
        if (this.components.get(unit) == null) {
            this.components.put(unit, new ComponentBuilder(unit));
        }
        final IVariableBinding parameterBinding = parameter.resolveBinding();
        if (parameterBinding == null) {
            LOG.warn("Unresolved parameter binding " + parameter.getName() + " detected in " + getFullUnitName(unit)
                    + "!");
            return;
        }
        final ITypeBinding type = parameterBinding.getType();
        final EntireInterface iface = new EntireInterface(type,
                new JavaInterfaceName(NameConverter.toPCMIdentifier(type)));
        this.detectRequiredInterface(unit, compositeRequired, false, iface);
    }

    private void detectRequiredInterface(final CompUnitOrName unit, final boolean compositeRequired,
            final boolean detectWeakly, final OperationInterface iface) {
        this.detectRequiredInterface(unit, compositeRequired, detectWeakly, List.of(iface));
    }

    private void detectRequiredInterface(final CompUnitOrName unit, final boolean compositeRequired,
            final boolean detectWeakly, final Collection<OperationInterface> ifaces) {
        for (final OperationInterface iface : ifaces) {
            final boolean isProvided = this.compositeProvisions.containsRelated(iface) || this.components.values()
                .stream()
                .anyMatch(component -> component.provisions()
                    .containsRelated(iface));
            if (!isProvided && detectWeakly) {
                this.components.get(unit)
                    .requirements()
                    .addWeakly(iface);
                if (compositeRequired) {
                    this.compositeRequirements.addWeakly(iface);
                }
            } else {
                this.components.get(unit)
                    .requirements()
                    .add(iface);
                this.components.values()
                    .stream()
                    .forEach(component -> component.provisions()
                        .strengthenIfPresent(iface));
                this.compositeProvisions.strengthenIfPresent(iface);
                if (compositeRequired) {
                    this.compositeRequirements.add(iface);
                }
            }
        }
    }

    public void detectProvidedInterface(final CompUnitOrName unit, final ITypeBinding iface) {
        this.detectProvidedInterface(unit, iface, false, false);
    }

    public void detectProvidedInterfaceWeakly(final CompUnitOrName unit, final ITypeBinding iface) {
        this.detectProvidedInterface(unit, iface, false, true);
    }

    private void detectProvidedInterface(final CompUnitOrName unit, final ITypeBinding iface,
            final boolean compositeProvided, final boolean detectWeakly) {
        if (iface == null) {
            LOG.warn("Unresolved type binding detected in " + getFullUnitName(unit) + "!");
            return;
        }
        final OperationInterface provision = new EntireInterface(iface,
                new JavaInterfaceName(NameConverter.toPCMIdentifier(iface)));
        this.detectProvidedInterface(unit, provision, compositeProvided, detectWeakly);
    }

    public void detectProvidedOperation(final CompUnitOrName unit, final IMethodBinding method) {
        detectProvidedOperation(unit, method, false);
    }

    public void detectProvidedOperationWeakly(final CompUnitOrName unit, final IMethodBinding method) {
        detectProvidedOperation(unit, method, true);
    }

    private void detectProvidedOperation(final CompUnitOrName unit, final IMethodBinding method,
            final boolean detectWeakly) {
        if (method == null) {
            LOG.warn("Unresolved method binding detected in " + getFullUnitName(unit) + "!");
            return;
        }
        this.detectProvidedOperation(unit, method.getDeclaringClass(), method, detectWeakly);
    }

    public void detectProvidedOperation(final CompUnitOrName unit, final ITypeBinding declaringIface,
            final IMethodBinding method) {
        this.detectProvidedOperation(unit, declaringIface, method, false);
    }

    public void detectProvidedOperationWeakly(final CompUnitOrName unit, final ITypeBinding declaringIface,
            final IMethodBinding method) {
        this.detectProvidedOperation(unit, declaringIface, method, true);
    }

    private void detectProvidedOperation(final CompUnitOrName unit, final ITypeBinding declaringIface,
            final IMethodBinding method, final boolean detectWeakly) {
        if (declaringIface == null) {
            LOG.warn("Unresolved type binding detected in " + getFullUnitName(unit) + "!");
            return;
        }
        this.detectProvidedOperation(unit, NameConverter.toPCMIdentifier(declaringIface), method, false, detectWeakly);
    }

    public void detectProvidedOperation(final CompUnitOrName unit, final String declaringIface,
            final IMethodBinding method) {
        this.detectProvidedOperation(unit, declaringIface, method, false, false);
    }

    private void detectProvidedOperation(final CompUnitOrName unit, final String declaringIface,
            final IMethodBinding method, final boolean compositeProvided, final boolean detectWeakly) {
        String operationName;
        if (method == null) {
            LOG.warn("Unresolved method binding detected in " + getFullUnitName(unit) + "!");
            operationName = "[unresolved]";
        } else {
            operationName = method.getName();
        }

        this.detectProvidedOperation(unit, method, new JavaOperationName(declaringIface, operationName),
                compositeProvided, detectWeakly);
    }

    public void detectProvidedOperation(final CompUnitOrName unit, final IMethodBinding method,
            final OperationName name) {
        detectProvidedOperation(unit, method, name, false, false);
    }

    private void detectProvidedOperation(final CompUnitOrName unit, final IMethodBinding method,
            final OperationName name, final boolean compositeProvided, final boolean detectWeakly) {
        if (this.components.get(unit) == null) {
            this.components.put(unit, new ComponentBuilder(unit));
        }
        final OperationInterface provision = new Operation(method, name);
        this.detectProvidedInterface(unit, provision, compositeProvided, detectWeakly);
    }

    private void detectProvidedInterface(final CompUnitOrName unit, final OperationInterface iface,
            final boolean compositeProvided, final boolean detectWeakly) {
        this.detectProvidedInterface(unit, compositeProvided, detectWeakly, List.of(iface));
    }

    private void detectProvidedInterface(final CompUnitOrName unit, final boolean compositeProvided,
            final boolean detectWeakly, final Collection<OperationInterface> ifaces) {
        for (final OperationInterface iface : ifaces) {
            final boolean isRequired = this.compositeRequirements.containsRelated(iface) || this.components.values()
                .stream()
                .anyMatch(component -> component.requirements()
                    .containsRelated(iface));
            if (!isRequired && detectWeakly) {
                this.components.get(unit)
                    .provisions()
                    .addWeakly(iface);
                if (compositeProvided) {
                    this.compositeProvisions.addWeakly(iface);
                }
            } else {
                this.components.get(unit)
                    .provisions()
                    .add(iface);
                this.components.values()
                    .stream()
                    .forEach(component -> component.requirements()
                        .strengthenIfPresent(iface));
                this.compositeRequirements.strengthenIfPresent(iface);
                if (compositeProvided) {
                    this.compositeProvisions.add(iface);
                }
            }
        }
    }

    public void detectPartOfComposite(final CompUnitOrName unit, final String compositeName) {
        if (this.components.get(unit) == null) {
            this.components.put(unit, new ComponentBuilder(unit));
        }
        this.getComposite(compositeName)
            .addPart(this.components.get(unit));
    }

    public void detectCompositeRequiredInterface(final CompUnitOrName unit, final InterfaceName interfaceName) {
        this.detectRequiredInterface(unit, interfaceName, true);
    }

    public void detectCompositeRequiredInterface(final CompUnitOrName unit, final FieldDeclaration field) {
        this.detectRequiredInterface(unit, field, true, false);
    }

    public void detectCompositeRequiredInterface(final CompUnitOrName unit, final SingleVariableDeclaration parameter) {
        this.detectRequiredInterface(unit, parameter, true);
    }

    public void detectCompositeProvidedOperation(final CompUnitOrName unit, final IMethodBinding method) {
        this.detectCompositeProvidedOperation(unit, method.getDeclaringClass(), method);
    }

    public void detectCompositeProvidedOperation(final CompUnitOrName unit, final ITypeBinding declaringIface,
            final IMethodBinding method) {
        this.detectCompositeProvidedOperation(unit, NameConverter.toPCMIdentifier(declaringIface), method, false);
    }

    private void detectCompositeProvidedOperation(final CompUnitOrName unit, final String declaringIface,
            final IMethodBinding method, final boolean detectWeakly) {
        this.detectProvidedOperation(unit, declaringIface, method, true, detectWeakly);
    }

    public void detectCompositeProvidedOperation(final CompUnitOrName unit, final IMethodBinding method,
            final OperationName name) {
        this.detectProvidedOperation(unit, method, name, true, false);
    }

    private CompositeBuilder getComposite(final String name) {
        if (!this.composites.containsKey(name)) {
            this.composites.put(name, new CompositeBuilder(name));
        }
        return this.composites.get(name);
    }

    public Set<CompUnitOrName> getCompilationUnits() {
        return this.components.keySet();
    }

    public PCMDetectionResult getResult() {
        return new PCMDetectionResult(this.components, this.composites, this.compositeProvisions,
                this.compositeRequirements);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(142);
        sb.append("[PCMDetector] {\n\tcomponents: {\n");
        this.components.entrySet()
            .forEach(entry -> {
                sb.append("\t\t\"");
                sb.append(entry.getKey());
                sb.append("\" -> {");
                sb.append(entry.getValue());
                sb.append("\t\t}\n");
            });
        return sb.toString();
    }
}
