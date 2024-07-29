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
import org.eclipse.jdt.core.dom.MethodInvocation;
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
    private final Map<CompUnitOrName, List<String>> weakComponents = new ConcurrentHashMap<>();
    private final Map<CompUnitOrName, String> separatingIdentifiers = new ConcurrentHashMap<>();

    private static String getFullUnitName(final CompUnitOrName unit) {
        // TODO this is potentially problematic, maybe restructure
        // On the other hand, it is still fit as a unique identifier,
        // since types cannot be declared multiple times.

        if (!unit.isUnit()) {
            return unit.name();
        }

        final List<String> names = new ArrayList<>();
        for (final Object type : unit.compilationUnit()
            .get()
            .types()) {
            if (type instanceof AbstractTypeDeclaration) {
                String fullTypeName = ((AbstractTypeDeclaration) type).getName()
                    .getFullyQualifiedName();
                names.add(fullTypeName);
            }
        }
        if (!names.isEmpty()) {
            return names.get(0);
        }

        return null;
    }

    public void detectComponent(final CompUnitOrName unit) {
        if (!unit.isUnit()) {
            if (this.components.get(unit) == null) {
                this.components.put(unit, new ComponentBuilder(unit));
            }
            return;
        }
        for (final Object type : unit.compilationUnit()
            .get()
            .types()) {
            if (type instanceof TypeDeclaration) {
                if (this.components.get(unit) == null) {
                    this.components.put(unit, new ComponentBuilder(unit));
                }
                final ITypeBinding binding = ((TypeDeclaration) type).resolveBinding();
                this.detectProvidedInterfaceWeakly(unit, binding);
            }
        }
    }

    public void detectRequiredInterface(final CompUnitOrName unit, final InterfaceName interfaceName) {
        this.detectRequiredInterface(unit, interfaceName, false);
    }

    private void detectRequiredInterface(final CompUnitOrName unit, final InterfaceName interfaceName,
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
        this.detectRequired(unit, compositeRequired, detectWeakly, ifaces);
    }

    public void detectCompositeRequiredInterfaceWeakly(final CompUnitOrName unit, final MethodInvocation invocation) {
        final IMethodBinding method = invocation.resolveMethodBinding();
        if (method == null) {
            return;
        }
        final ITypeBinding type = method.getDeclaringClass();
        if (this.components.get(unit) == null) {
            this.components.put(unit, new ComponentBuilder(unit));
        }
        final EntireInterface iface = new EntireInterface(type,
                new JavaInterfaceName(NameConverter.toPCMIdentifier(type)));
        this.detectRequiredInterface(unit, true, true, iface);
    }

    public void detectRequiredInterface(final CompUnitOrName unit, final SingleVariableDeclaration parameter) {
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
        this.detectRequiredInterface(unit, false, false, iface);
    }

    private void detectRequiredInterface(final CompUnitOrName unit, final boolean compositeRequired,
            final boolean detectWeakly, final OperationInterface iface) {
        this.detectRequired(unit, compositeRequired, detectWeakly, List.of(iface));
    }

    private void detectRequired(final CompUnitOrName unit, final boolean compositeRequired, final boolean detectWeakly,
            final Collection<OperationInterface> ifaces) {
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

    public void detectProvidedInterfaceWeakly(final CompUnitOrName unit, final ITypeBinding iface) {
        if (iface == null) {
            LOG.warn("Unresolved type binding detected in " + getFullUnitName(unit) + "!");
            return;
        }
        final OperationInterface provision = new EntireInterface(iface,
                new JavaInterfaceName(NameConverter.toPCMIdentifier(iface)));
        this.detectProvidedInterface(unit, provision, false, true);
    }

    public void detectProvidedOperationWeakly(final CompUnitOrName unit, final IMethodBinding method) {
        if (method == null) {
            LOG.warn("Unresolved method binding detected in " + getFullUnitName(unit) + "!");
            return;
        }
        this.detectProvidedOperation(unit, method.getDeclaringClass(), method, true);
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

        String operationName;
        if (method == null) {
            LOG.warn("Unresolved method binding detected in " + getFullUnitName(unit) + "!");
            operationName = "[unresolved]";
        } else {
            operationName = method.getName();
        }

        this.detectProvidedOperation(unit, method,
                new JavaOperationName(NameConverter.toPCMIdentifier(declaringIface), operationName), false,
                detectWeakly);
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

    public void detectSeparatingIdentifier(final CompUnitOrName unit, final String separatingIdentifier) {
        if (this.components.get(unit) == null) {
            this.separatingIdentifiers.put(unit, separatingIdentifier);
        } else {
            this.components.get(unit)
                .setSeparatingIdentifier(separatingIdentifier);
        }
    }

    public void detectPartOfComposite(final CompUnitOrName unit, final String compositeName) {
        if (this.components.get(unit) == null) {
            this.components.put(unit, new ComponentBuilder(unit));
        }
        if (!this.composites.containsKey(compositeName)) {
            this.composites.put(compositeName, new CompositeBuilder(compositeName));
        }
        this.composites.get(compositeName)
            .addPart(this.components.get(unit));

        // Setting the separating identifier is sufficient if the component is part of a composite
        if (separatingIdentifiers.containsKey(unit)) {
            this.components.get(unit)
                .setSeparatingIdentifier(separatingIdentifiers.get(unit));
        }

        // Realize weak composite components that this component is part of
        if (this.weakComponents.containsKey(unit)) {
            for (String weakCompositeName : weakComponents.get(unit)) {
                if (!this.composites.containsKey(weakCompositeName)) {
                    this.composites.put(weakCompositeName, new CompositeBuilder(weakCompositeName));
                }
                this.composites.get(weakCompositeName)
                    .addPart(this.components.get(unit));
            }
        }
    }

    // Weak composite components only get created if at least one of their components is part of
    // another composite. This allows for e.g. composite components based on build files that
    // do not require direct dependencies between their parts.
    public void detectPartOfWeakComposite(CompUnitOrName unit, String compositeName) {
        if (!this.weakComponents.containsKey(unit)) {
            this.weakComponents.put(unit, new ArrayList<>());
        }
        this.weakComponents.get(unit)
            .add(compositeName);

        final boolean isPartOfStrongComposite = this.composites.values()
            .stream()
            .anyMatch(x -> x.hasPart(unit));
        if (isPartOfStrongComposite) {
            if (!this.composites.containsKey(compositeName)) {
                this.composites.put(compositeName, new CompositeBuilder(compositeName));
            }
            this.composites.get(compositeName)
                .addPart(this.components.get(unit));
        }
    }

    public void detectCompositeRequiredInterface(final CompUnitOrName unit, final InterfaceName interfaceName) {
        this.detectRequiredInterface(unit, interfaceName, true);
    }

    public void detectCompositeProvidedOperation(final CompUnitOrName unit, final IMethodBinding method,
            final OperationName name) {
        this.detectProvidedOperation(unit, method, name, true, false);
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

    public boolean isPartOfComposite(CompUnitOrName name) {
        for (CompositeBuilder composite : this.composites.values()) {
            if (composite.hasPart(name)) {
                return true;
            }
        }
        return false;
    }
}
