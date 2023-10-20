package org.palladiosimulator.somox.analyzer.rules.engine;

import java.util.ArrayList;
import java.util.HashMap;
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
import org.palladiosimulator.somox.analyzer.rules.model.CompUnitOrName;
import org.palladiosimulator.somox.analyzer.rules.model.ComponentBuilder;
import org.palladiosimulator.somox.analyzer.rules.model.CompositeBuilder;
import org.palladiosimulator.somox.analyzer.rules.model.EntireInterface;
import org.palladiosimulator.somox.analyzer.rules.model.InterfaceName;
import org.palladiosimulator.somox.analyzer.rules.model.JavaInterfaceName;
import org.palladiosimulator.somox.analyzer.rules.model.JavaOperationName;
import org.palladiosimulator.somox.analyzer.rules.model.Operation;
import org.palladiosimulator.somox.analyzer.rules.model.OperationName;
import org.palladiosimulator.somox.analyzer.rules.model.PCMDetectionResult;
import org.palladiosimulator.somox.analyzer.rules.model.ProvisionsBuilder;
import org.palladiosimulator.somox.analyzer.rules.model.RequirementsBuilder;

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
        components.get(unit)
            .requirements()
            .add(iface);
        if (compositeRequired) {
            compositeRequirements.add(iface);
        }
    }

    public void detectRequiredInterface(CompUnitOrName unit, FieldDeclaration field) {
        detectRequiredInterface(unit, field, false);
    }

    private void detectRequiredInterface(CompUnitOrName unit, FieldDeclaration field, boolean compositeRequired) {
        if (components.get(unit) == null) {
            components.put(unit, new ComponentBuilder(unit));
        }
        @SuppressWarnings("unchecked")
        List<EntireInterface> ifaces = ((List<VariableDeclaration>) field.fragments()).stream()
            .map(x -> x.resolveBinding())
            .filter(x -> x != null)
            .map(x -> x.getType())
            .map(x -> new EntireInterface(x, new JavaInterfaceName(NameConverter.toPCMIdentifier(x))))
            .collect(Collectors.toList());
        components.get(unit)
            .requirements()
            .add(ifaces);
        if (compositeRequired) {
            compositeRequirements.add(ifaces);
        }
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
        components.get(unit)
            .requirements()
            .add(iface);
        if (compositeRequired) {
            compositeRequirements.add(iface);
        }
    }

    public void detectProvidedInterface(CompUnitOrName unit, ITypeBinding iface) {
        if (iface == null) {
            LOG.warn("Unresolved type binding detected in " + getFullUnitName(unit) + "!");
            return;
        }
        components.get(unit)
            .provisions()
            .add(new EntireInterface(iface, new JavaInterfaceName(NameConverter.toPCMIdentifier(iface))));
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
        components.get(unit)
            .provisions()
            .add(new Operation(method, name));
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
        detectRequiredInterface(unit, field, true);
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
