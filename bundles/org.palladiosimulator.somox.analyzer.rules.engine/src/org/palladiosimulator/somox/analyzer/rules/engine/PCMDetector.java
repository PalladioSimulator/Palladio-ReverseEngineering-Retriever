package org.palladiosimulator.somox.analyzer.rules.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.palladiosimulator.somox.analyzer.rules.model.Component;
import org.palladiosimulator.somox.analyzer.rules.model.ComponentBuilder;
import org.palladiosimulator.somox.analyzer.rules.model.Composite;
import org.palladiosimulator.somox.analyzer.rules.model.CompositeBuilder;
import org.palladiosimulator.somox.analyzer.rules.model.EntireInterface;
import org.palladiosimulator.somox.analyzer.rules.model.InterfaceName;
import org.palladiosimulator.somox.analyzer.rules.model.JavaInterfaceName;
import org.palladiosimulator.somox.analyzer.rules.model.JavaOperationName;
import org.palladiosimulator.somox.analyzer.rules.model.Operation;
import org.palladiosimulator.somox.analyzer.rules.model.OperationInterface;
import org.palladiosimulator.somox.analyzer.rules.model.OperationName;
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

    private Map<CompilationUnit, ComponentBuilder> components = new HashMap<>();
    private Map<String, CompositeBuilder> composites = new HashMap<>();
    private ProvisionsBuilder compositeProvisions = new ProvisionsBuilder();
    private RequirementsBuilder compositeRequirements = new RequirementsBuilder();

    private Set<Component> constructedComponents = new HashSet<>();
    private Set<Composite> constructedComposites = new HashSet<>();

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
                ITypeBinding binding = ((TypeDeclaration) type).resolveBinding();
                detectProvidedInterface(unit, binding);
            }
        }
    }

    public void detectRequiredInterface(CompilationUnit unit, InterfaceName interfaceName) {
        detectRequiredInterface(unit, interfaceName, false);
    }

    public void detectRequiredInterface(CompilationUnit unit, InterfaceName interfaceName, boolean compositeRequired) {
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

    public void detectRequiredInterface(CompilationUnit unit, FieldDeclaration field) {
        detectRequiredInterface(unit, field, false);
    }

    private void detectRequiredInterface(CompilationUnit unit, FieldDeclaration field, boolean compositeRequired) {
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

    public void detectRequiredInterface(CompilationUnit unit, SingleVariableDeclaration parameter) {
        detectRequiredInterface(unit, parameter, false);
    }

    private void detectRequiredInterface(CompilationUnit unit, SingleVariableDeclaration parameter,
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

    public void detectProvidedInterface(CompilationUnit unit, ITypeBinding iface) {
        if (iface == null) {
            LOG.warn("Unresolved type binding detected in " + getFullUnitName(unit) + "!");
            return;
        }
        components.get(unit)
            .provisions()
            .add(new EntireInterface(iface, new JavaInterfaceName(NameConverter.toPCMIdentifier(iface))));
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
        String operationName;
        if (method == null) {
            LOG.warn("Unresolved method binding detected in " + getFullUnitName(unit) + "!");
            operationName = "[unresolved]";
        } else {
            operationName = method.getName();
        }

        detectProvidedOperation(unit, method, new JavaOperationName(declaringIface, operationName));
    }

    public void detectProvidedOperation(CompilationUnit unit, IMethodBinding method, OperationName name) {
        if (components.get(unit) == null) {
            components.put(unit, new ComponentBuilder(unit));
        }
        components.get(unit)
            .provisions()
            .add(new Operation(method, name));
    }

    public void detectPartOfComposite(CompilationUnit unit, String compositeName) {
        if (components.get(unit) == null) {
            components.put(unit, new ComponentBuilder(unit));
        }
        getComposite(compositeName).addPart(components.get(unit));
    }

    public void detectCompositeRequiredInterface(CompilationUnit unit, InterfaceName interfaceName) {
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
        detectCompositeProvidedOperation(unit, NameConverter.toPCMIdentifier(declaringIface), method);
    }

    public void detectCompositeProvidedOperation(CompilationUnit unit, String declaringIface, IMethodBinding method) {
        compositeProvisions.add(new Operation(method, new JavaOperationName(declaringIface, method.getName())));
        detectProvidedOperation(unit, declaringIface, method);
    }

    public void detectCompositeProvidedOperation(CompilationUnit unit, IMethodBinding method, OperationName name) {
        compositeProvisions.add(new Operation(method, name));
        detectProvidedOperation(unit, method, name);
    }

    private CompositeBuilder getComposite(String name) {
        if (!composites.containsKey(name)) {
            composites.put(name, new CompositeBuilder(name));
        }
        return composites.get(name);
    }

    public Set<CompilationUnit> getCompilationUnits() {
        return components.keySet();
    }

    protected Set<Component> getComponents() {
        if (constructedComponents.isEmpty()) {
            List<OperationInterface> allDependencies = new LinkedList<>();
            allDependencies.addAll(compositeRequirements.toList());
            allDependencies.addAll(compositeProvisions.toList());

            constructedComponents = components.values()
                .stream()
                .map(x -> x.create(allDependencies))
                .collect(Collectors.toSet());
        }
        return constructedComponents;
    }

    protected Map<String, List<Operation>> getOperationInterfaces() {
        // TODO: This has to include composite interfaces as well
        List<Map<String, List<Operation>>> operationInterfaces = getComponents().stream()
            .map(x -> x.provisions()
                .simplified())
            .collect(Collectors.toList());
        getComponents().stream()
            .map(x -> x.requirements()
                .simplified())
            .forEach(x -> operationInterfaces.add(x));
        getCompositeComponents().stream()
            .map(x -> x.provisions())
            .forEach(x -> operationInterfaces.add(x));
        getCompositeComponents().stream()
            .map(x -> x.requirements())
            .forEach(x -> operationInterfaces.add(x));
        return MapMerger.merge(operationInterfaces);
    }

    protected Set<Composite> getCompositeComponents() {
        // Construct composites.
        if (constructedComposites.isEmpty()) {
            List<Composite> allComposites = composites.values()
                .stream()
                .map(x -> x.construct(getComponents(), compositeRequirements.create(),
                        compositeProvisions.create(List.of())))
                .collect(Collectors.toList());

            // Remove redundant composites.
            Set<Composite> redundantComposites = new HashSet<>();
            for (int i = 0; i < allComposites.size(); ++i) {
                Composite subject = allComposites.get(i);
                long subsetCount = allComposites.subList(i + 1, allComposites.size())
                    .stream()
                    .filter(x -> subject.isSubsetOf(x) || x.isSubsetOf(subject))
                    .count();

                // Any composite is guaranteed to be the subset of at least one composite in the
                // list,
                // namely itself. If it is the subset of any composites other than itself, it is
                // redundant.
                if (subsetCount > 0) {
                    redundantComposites.add(subject);
                }

                // TODO: Is there any merging necessary, like adapting the redundant composite's
                // requirements to its peer?
                constructedComposites = allComposites.stream()
                    .filter(x -> !redundantComposites.contains(x))
                    .collect(Collectors.toUnmodifiableSet());
            }
        }
        return constructedComposites;
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
        return sb.toString();
    }
}
