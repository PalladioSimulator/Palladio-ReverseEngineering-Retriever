package org.palladiosimulator.somox.analyzer.rules.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.palladiosimulator.somox.analyzer.rules.blackboard.CompilationUnitWrapper;
import org.palladiosimulator.somox.analyzer.rules.model.Component;
import org.palladiosimulator.somox.analyzer.rules.model.ComponentBuilder;
import org.palladiosimulator.somox.analyzer.rules.model.JavaName;
import org.palladiosimulator.somox.analyzer.rules.model.Operation;

/**
 * This class is used to detect and hold all relevant elements found during the processing of rules.
 * It provides methods to detect and retrieve PCM elements. After all rules are parsed, this class
 * holds the results as "simple" java objects not yet transformed to real PCM objects like PCM Basic
 * Components.
 */
public class EclipsePCMDetector implements IPCMDetector {
    private static final Logger LOG = Logger.getLogger(EclipsePCMDetector.class);

    private Map<CompilationUnit, ComponentBuilder> components = new HashMap<>();
    private Map<String, List<IMethodBinding>> operationInterfaces = new HashMap<>();

    private Map<String, CompositeBuilder> composites = new HashMap<>();
    private Set<Operation> compositeProvidedOperations = new HashSet<>();
    private Set<String> compositeRequiredInterfaces = new HashSet<>();

    private static String getFullUnitName(CompilationUnit unit) {
        // TODO this is potentially problematic, maybe restructure
        // On the other hand, it is still fit as a unique identifier,
        // since types cannot be declared multiple times.

        List<String> names = getFullUnitNames(unit);
        if (!names.isEmpty()) {
            return names.get(0);
        }
        return null;
    }

    private static List<String> getFullUnitNames(CompilationUnit unit) {
        List<String> names = new ArrayList<>();
        for (Object type : unit.types()) {
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

    public void detectComponent(CompilationUnit unit) {
        for (Object type : unit.types()) {
            if (type instanceof TypeDeclaration) {
                components.put(unit, new ComponentBuilder(unit));
                // TODO: remove and implement properly
                ITypeBinding binding = ((TypeDeclaration) type).resolveBinding();
                detectOperationInterface(unit);
                detectProvidedOperation(unit, binding, null);
            }
        }
    }

    public void detectOperationInterface(CompilationUnit unit) {
        for (Object type : unit.types()) {
            if (type instanceof AbstractTypeDeclaration) {
                detectOperationInterface((AbstractTypeDeclaration) type);
            }
        }

    }

    public void detectOperationInterface(CompilationUnit unit, String overrideName) {
        for (Object type : unit.types()) {
            if (type instanceof AbstractTypeDeclaration) {
                detectOperationInterface((AbstractTypeDeclaration) type, overrideName);
            }
        }
    }

    private void detectOperationInterface(AbstractTypeDeclaration type) {
        detectOperationInterface(type, NameConverter.toPCMIdentifier(type.resolveBinding()));
    }

    private void detectOperationInterface(AbstractTypeDeclaration type, String overrideName) {
        if (type instanceof TypeDeclaration) {
            detectOperationInterface(type.resolveBinding(), overrideName);
        }
    }

    private void detectOperationInterface(ITypeBinding binding) {
        detectOperationInterface(binding, NameConverter.toPCMIdentifier(binding));
    }

    private void detectOperationInterface(ITypeBinding binding, String overrideName) {
        if (binding == null) {
            LOG.warn("Unresolved interface binding detected in " + overrideName + "!");
            return;
        }
        if (binding.isClass() || binding.isInterface()) {
            operationInterfaces.put(overrideName, List.of(binding.getTypeDeclaration()
                .getDeclaredMethods()));
        }
    }

    public void detectOperationInterface(Type type) {
        ITypeBinding binding = type.resolveBinding();
        if (binding == null) {
            LOG.warn("Unresolved interface binding detected!");
            return;
        }
        detectOperationInterface(binding.getTypeDeclaration());
    }

    public void detectRequiredInterface(CompilationUnit unit, String interfaceName) {
        detectRequiredInterface(unit, interfaceName, false);
    }

    public void detectRequiredInterface(CompilationUnit unit, String interfaceName, boolean compositeRequired) {
        if (components.get(unit) == null) {
            components.put(unit, new ComponentBuilder(unit));
        }
        components.get(unit)
            .requirements()
            .add(interfaceName);
        if (compositeRequired) {
            addCompositeRequiredInterface(interfaceName);
        }
        detectOperationInterface(unit, interfaceName);
    }

    public void detectRequiredInterface(CompilationUnit unit, FieldDeclaration field) {
        detectRequiredInterface(unit, field, false);
    }

    private void detectRequiredInterface(CompilationUnit unit, FieldDeclaration field, boolean compositeRequired) {
        if (components.get(unit) == null) {
            components.put(unit, new ComponentBuilder(unit));
        }
        @SuppressWarnings("unchecked")
        List<String> ifaceNames = ((List<VariableDeclaration>) field.fragments()).stream()
            .map(x -> x.resolveBinding()
                .getType())
            .map(NameConverter::toPCMIdentifier)
            .collect(Collectors.toList());
        components.get(unit)
            .requirements()
            .add(ifaceNames);
        detectOperationInterface(field.getType());
        if (compositeRequired) {
            for (String ifaceName : ifaceNames) {
                addCompositeRequiredInterface(ifaceName);
            }
        }
    }

    public void detectRequiredInterface(CompilationUnit unit, SingleVariableDeclaration parameter) {
        detectRequiredInterface(unit, parameter, false);
    }

    private void detectRequiredInterface(CompilationUnit unit, SingleVariableDeclaration parameter,
            boolean compositeRequired) {
        if (components.get(unit) == null) {
            components.put(unit, new ComponentBuilder(unit));
        }
        String ifaceName = NameConverter.toPCMIdentifier(parameter.resolveBinding()
            .getType());
        components.get(unit)
            .requirements()
            .add(ifaceName);
        detectOperationInterface(parameter.getType());
        if (compositeRequired) {
            addCompositeRequiredInterface(ifaceName);
        }
    }

    public void detectProvidedOperation(CompilationUnit unit, IMethodBinding method) {
        if (method == null) {
            LOG.warn("Unresolved method binding detected in " + getFullUnitName(unit) + "!");
            return;
        }
        detectProvidedOperation(unit, method.getDeclaringClass(), method);
    }

    public void detectProvidedOperation(CompilationUnit unit, ITypeBinding declaringIface, IMethodBinding method) {
        if (declaringIface == null) {
            LOG.warn("Unresolved type binding detected in " + getFullUnitName(unit) + "!");
            return;
        }
        detectProvidedOperation(unit, NameConverter.toPCMIdentifier(declaringIface), method);
    }

    public void detectProvidedOperation(CompilationUnit unit, String declaringIface, IMethodBinding method) {
        if (components.get(unit) == null) {
            components.put(unit, new ComponentBuilder(unit));
        }
        String operationName;
        if (method == null) {
            LOG.warn("Unresolved method binding detected in " + getFullUnitName(unit) + "!");
            operationName = "[unresolved]";
        } else {
            operationName = method.getName();
        }
        components.get(unit)
            .provisions()
            .add(new Operation(method, new JavaName(declaringIface, operationName)));
    }

    public void detectPartOfComposite(CompilationUnit unit, String compositeName) {
        if (components.get(unit) == null) {
            components.put(unit, new ComponentBuilder(unit));
        }
        getComposite(compositeName).addPart(components.get(unit));
    }

    public void detectCompositeRequiredInterface(CompilationUnit unit, String interfaceName) {
        detectRequiredInterface(unit, interfaceName, true);
    }

    public void detectCompositeRequiredInterface(CompilationUnit unit, FieldDeclaration field) {
        detectRequiredInterface(unit, field, true);
    }

    public void detectCompositeRequiredInterface(CompilationUnit unit, SingleVariableDeclaration parameter) {
        detectRequiredInterface(unit, parameter, true);
    }

    public void detectCompositeProvidedOperation(CompilationUnit unit, IMethodBinding method) {
        detectCompositeProvidedOperation(unit, method.getDeclaringClass(), method);
    }

    public void detectCompositeProvidedOperation(CompilationUnit unit, ITypeBinding declaringIface,
            IMethodBinding method) {
        String declaringIfaceName = NameConverter.toPCMIdentifier(declaringIface);
        detectCompositeProvidedOperation(unit, declaringIfaceName, method);
    }

    public void detectCompositeProvidedOperation(CompilationUnit unit, String declaringIface, IMethodBinding method) {
        addCompositeProvidedOperation(new Operation(method, new JavaName(declaringIface, method.getName())));
        detectProvidedOperation(unit, declaringIface, method);
    }

    private void addCompositeRequiredInterface(String ifaceName) {
        compositeRequiredInterfaces.add(ifaceName);
    }

    private void addCompositeProvidedOperation(Operation operation) {
        compositeProvidedOperations.add(operation);
    }

    private CompositeBuilder getComposite(String name) {
        if (!composites.containsKey(name)) {
            composites.put(name, new CompositeBuilder(name));
        }
        return composites.get(name);
    }

    @Override
    public List<CompilationUnitWrapper> getWrappedComponents() {
        return CompilationUnitWrapper.wrap(components.keySet());
    }

    protected List<Component> getComponents() {
        return List.of(components.values()
            .toArray(new Component[0]));
    }

    protected Map<String, List<IMethodBinding>> getOperationInterfaces() {
        // TODO: Map the component representation to a list of interfaces with
        // (potentially renamed) methods.
        return Collections.unmodifiableMap(operationInterfaces);
    }

    protected Set<Composite> getCompositeComponents() {
        // Construct composites.
        List<Composite> constructedComposites = composites.values()
            .stream()
            .map(x -> x.construct(components.values(), compositeRequiredInterfaces, compositeProvidedOperations))
            .collect(Collectors.toList());

        // Remove redundant composites.
        Set<Composite> redundantComposites = new HashSet<>();
        for (int i = 0; i < constructedComposites.size(); ++i) {
            Composite subject = constructedComposites.get(i);
            long subsetCount = constructedComposites.subList(i + 1, constructedComposites.size())
                .stream()
                .filter(x -> subject.isSubsetOf(x) || x.isSubsetOf(subject))
                .count();

            // Any composite is guaranteed to be the subset of at least one composite in the list,
            // namely itself. If it is the subset of any composites other than itself, it is
            // redundant.
            if (subsetCount > 0) {
                redundantComposites.add(subject);
            }

            // TODO: Is there any merging necessary, like adapting the redundant composite's
            // requirements to its peer?
        }

        return constructedComposites.stream()
            .filter(x -> !redundantComposites.contains(x))
            .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(149);
        sb.append("[EclipsePCMDetector] {\n\tcomponents: {\n");
        components.entrySet()
            .forEach(entry -> {
                sb.append("\t\t\"");
                sb.append(entry.getKey());
                sb.append("\" -> {");
                sb.append(entry.getValue());
                sb.append("\t\t}\n");
            });

        sb.append("\t}\n\tinterfaces: {\n");
        operationInterfaces.forEach((iface, op) -> {
            sb.append("\t\t")
                .append(iface)
                .append(":\n");
            op.forEach(y -> sb.append("\t\t\t")
                .append(y.getName())
                .append('\n'));
            sb.append('\n');
        });

        return sb.toString();
    }
}
