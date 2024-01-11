package org.palladiosimulator.retriever.extraction.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private Map<CompUnitOrName, ComponentBuilder> components = new HashMap<>();
    private Map<String, CompositeBuilder> composites = new HashMap<>();
    private ProvisionsBuilder compositeProvisions = new ProvisionsBuilder();
    private RequirementsBuilder compositeRequirements = new RequirementsBuilder();
    private Set<OperationInterface> providedInterfaces = new HashSet<>();

    private static String getFullUnitName(CompUnitOrName unit) {
        // TODO this is potentially problematic, maybe restructure
        // On the other hand, it is still fit as a unique identifier,
        // since types cannot be declared multiple times.

        List<String> names = getFullUnitNames(unit);
        if (!names.isEmpty()) {
            return names.get(0);
        }
        return null;
    }

    private static List<String> getFullUnitNames(CompUnitOrName unit) {
        if (!unit.isUnit()) {
            return List.of(unit.name());
        }

        List<String> names = new ArrayList<>();
        for (Object type : unit.compilationUnit()
            .get()
            .types()) {
            if (type instanceof AbstractTypeDeclaration) {
                names.add(getFullTypeName((AbstractTypeDeclaration) type));
            }
        }

        return names;
    }

    private static String getFullTypeName(AbstractTypeDeclaration type) {
        return type.getName()
            .getFullyQualifiedName();
    }

    public void detectComponent(CompUnitOrName unit) {
        if (!unit.isUnit()) {
            components.put(unit, new ComponentBuilder(unit));
            return;
        }
        for (Object type : unit.compilationUnit()
            .get()
            .types()) {
            if (type instanceof TypeDeclaration) {
                components.put(unit, new ComponentBuilder(unit));
                ITypeBinding binding = ((TypeDeclaration) type).resolveBinding();
                detectProvidedInterface(unit, binding);
            }
        }
    }

    public void detectRequiredInterface(CompUnitOrName unit, InterfaceName interfaceName) {
        detectRequiredInterface(unit, interfaceName, false);
    }

    public void detectRequiredInterface(CompUnitOrName unit, InterfaceName interfaceName, boolean compositeRequired) {
        if (components.get(unit) == null) {
            components.put(unit, new ComponentBuilder(unit));
        }
        EntireInterface iface = new EntireInterface(interfaceName);
        detectRequiredInterface(unit, compositeRequired, false, iface);
    }

    public void detectRequiredInterface(CompUnitOrName unit, FieldDeclaration field) {
        detectRequiredInterface(unit, field, false, false);
    }

    public void detectRequiredInterfaceWeakly(CompUnitOrName unit, FieldDeclaration field) {
        detectRequiredInterface(unit, field, false, true);
    }

    private void detectRequiredInterface(CompUnitOrName unit, FieldDeclaration field, boolean compositeRequired,
            boolean detectWeakly) {
        if (components.get(unit) == null) {
            components.put(unit, new ComponentBuilder(unit));
        }
        @SuppressWarnings("unchecked")
        List<OperationInterface> ifaces = ((List<VariableDeclaration>) field.fragments()).stream()
            .map(x -> x.resolveBinding())
            .filter(x -> x != null)
            .map(x -> x.getType())
            .map(x -> new EntireInterface(x, new JavaInterfaceName(NameConverter.toPCMIdentifier(x))))
            .collect(Collectors.toList());
        detectRequiredInterface(unit, compositeRequired, detectWeakly, ifaces);
    }

    public void detectRequiredInterface(CompUnitOrName unit, SingleVariableDeclaration parameter) {
        detectRequiredInterface(unit, parameter, false);
    }

    private void detectRequiredInterface(CompUnitOrName unit, SingleVariableDeclaration parameter,
            boolean compositeRequired) {
        if (components.get(unit) == null) {
            components.put(unit, new ComponentBuilder(unit));
        }
        IVariableBinding parameterBinding = parameter.resolveBinding();
        if (parameterBinding == null) {
            LOG.warn("Unresolved parameter binding " + parameter.getName() + " detected in " + getFullUnitName(unit)
                    + "!");
            return;
        }
        ITypeBinding type = parameterBinding.getType();
        EntireInterface iface = new EntireInterface(type, new JavaInterfaceName(NameConverter.toPCMIdentifier(type)));
        detectRequiredInterface(unit, compositeRequired, false, iface);
    }

    private void detectRequiredInterface(CompUnitOrName unit, boolean compositeRequired, boolean detectWeakly,
            OperationInterface iface) {
        detectRequiredInterface(unit, compositeRequired, detectWeakly, List.of(iface));
    }

    private void detectRequiredInterface(CompUnitOrName unit, boolean compositeRequired, boolean detectWeakly,
            Collection<OperationInterface> ifaces) {
        for (OperationInterface iface : ifaces) {
            if (detectWeakly && !providedInterfaces.contains(iface)) {
                components.get(unit)
                    .requirements()
                    .addWeakly(iface);
                if (compositeRequired) {
                    compositeRequirements.addWeakly(iface);
                }
            } else {
                components.get(unit)
                    .requirements()
                    .add(iface);
                if (compositeRequired) {
                    compositeRequirements.add(iface);
                }
            }
        }
    }

    public void detectProvidedInterface(CompUnitOrName unit, ITypeBinding iface) {
        if (iface == null) {
            LOG.warn("Unresolved type binding detected in " + getFullUnitName(unit) + "!");
            return;
        }
        OperationInterface provision = new EntireInterface(iface,
                new JavaInterfaceName(NameConverter.toPCMIdentifier(iface)));
        detectProvidedInterface(unit, provision);
    }

    public void detectProvidedOperation(CompUnitOrName unit, IMethodBinding method) {
        if (method == null) {
            LOG.warn("Unresolved method binding detected in " + getFullUnitName(unit) + "!");
            return;
        }
        detectProvidedOperation(unit, method.getDeclaringClass(), method);
    }

    public void detectProvidedOperation(CompUnitOrName unit, ITypeBinding declaringIface, IMethodBinding method) {
        if (declaringIface == null) {
            LOG.warn("Unresolved type binding detected in " + getFullUnitName(unit) + "!");
            return;
        }
        detectProvidedOperation(unit, NameConverter.toPCMIdentifier(declaringIface), method);
    }

    public void detectProvidedOperation(CompUnitOrName unit, String declaringIface, IMethodBinding method) {
        String operationName;
        if (method == null) {
            LOG.warn("Unresolved method binding detected in " + getFullUnitName(unit) + "!");
            operationName = "[unresolved]";
        } else {
            operationName = method.getName();
        }

        detectProvidedOperation(unit, method, new JavaOperationName(declaringIface, operationName));
    }

    public void detectProvidedOperation(CompUnitOrName unit, IMethodBinding method, OperationName name) {
        if (components.get(unit) == null) {
            components.put(unit, new ComponentBuilder(unit));
        }
        OperationInterface provision = new Operation(method, name);
        detectProvidedInterface(unit, provision);
    }

    public void detectProvidedInterface(CompUnitOrName unit, OperationInterface provision) {
        components.get(unit)
            .provisions()
            .add(provision);
        providedInterfaces.add(provision);
        components.values()
            .stream()
            .forEach(component -> component.requirements()
                .strengthenIfPresent(provision));
    }

    public void detectPartOfComposite(CompUnitOrName unit, String compositeName) {
        if (components.get(unit) == null) {
            components.put(unit, new ComponentBuilder(unit));
        }
        getComposite(compositeName).addPart(components.get(unit));
    }

    public void detectCompositeRequiredInterface(CompUnitOrName unit, InterfaceName interfaceName) {
        detectRequiredInterface(unit, interfaceName, true);
    }

    public void detectCompositeRequiredInterface(CompUnitOrName unit, FieldDeclaration field) {
        detectRequiredInterface(unit, field, true, false);
    }

    public void detectCompositeRequiredInterface(CompUnitOrName unit, SingleVariableDeclaration parameter) {
        detectRequiredInterface(unit, parameter, true);
    }

    public void detectCompositeProvidedOperation(CompUnitOrName unit, IMethodBinding method) {
        detectCompositeProvidedOperation(unit, method.getDeclaringClass(), method);
    }

    public void detectCompositeProvidedOperation(CompUnitOrName unit, ITypeBinding declaringIface,
            IMethodBinding method) {
        detectCompositeProvidedOperation(unit, NameConverter.toPCMIdentifier(declaringIface), method);
    }

    public void detectCompositeProvidedOperation(CompUnitOrName unit, String declaringIface, IMethodBinding method) {
        compositeProvisions.add(new Operation(method, new JavaOperationName(declaringIface, method.getName())));
        detectProvidedOperation(unit, declaringIface, method);
    }

    public void detectCompositeProvidedOperation(CompUnitOrName unit, IMethodBinding method, OperationName name) {
        compositeProvisions.add(new Operation(method, name));
        detectProvidedOperation(unit, method, name);
    }

    private CompositeBuilder getComposite(String name) {
        if (!composites.containsKey(name)) {
            composites.put(name, new CompositeBuilder(name));
        }
        return composites.get(name);
    }

    public Set<CompUnitOrName> getCompilationUnits() {
        return components.keySet();
    }

    public PCMDetectionResult getResult() {
        return new PCMDetectionResult(components, composites, compositeProvisions, compositeRequirements);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(142);
        sb.append("[PCMDetector] {\n\tcomponents: {\n");
        components.entrySet()
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
