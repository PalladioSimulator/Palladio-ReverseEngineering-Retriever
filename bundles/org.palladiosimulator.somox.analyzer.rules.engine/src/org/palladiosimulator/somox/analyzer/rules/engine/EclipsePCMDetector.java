package org.palladiosimulator.somox.analyzer.rules.engine;

import java.util.ArrayList;
import java.util.Collection;
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

/**
 * This class is used to detect and hold all relevant elements found during the processing of rules.
 * It provides methods to detect and retrieve PCM elements. After all rules are parsed, this class
 * holds the results as "simple" java objects not yet transformed to real PCM objects like PCM Basic
 * Components.
 */
public class EclipsePCMDetector implements IPCMDetector {
    private static final Logger LOG = Logger.getLogger(EclipsePCMDetector.class);
    private List<CompilationUnit> components = new ArrayList<>();

    private Map<CompilationUnit, Set<String>> providedInterfaces = new HashMap<>();

    private Map<CompilationUnit, Set<String>> requiredInterfaces = new HashMap<>();

    private Map<String, List<IMethodBinding>> operationInterfaces = new HashMap<>();

    private Map<String, CompositeBuilder> composites = new HashMap<>();

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
                components.add(unit);
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
        if (requiredInterfaces.get(unit) == null) {
            requiredInterfaces.put(unit, new HashSet<>());
        }
        requiredInterfaces.get(unit)
            .add(interfaceName);
        detectOperationInterface(unit, interfaceName);
    }

    public void detectRequiredInterface(CompilationUnit unit, FieldDeclaration field) {
        detectRequiredInterface(unit, field, null);
    }

    private void detectRequiredInterface(CompilationUnit unit, FieldDeclaration field, String compositeName) {
        if (requiredInterfaces.get(unit) == null) {
            requiredInterfaces.put(unit, new HashSet<>());
        }
        @SuppressWarnings("unchecked")
        List<String> ifaceNames = ((List<VariableDeclaration>) field.fragments()).stream()
            .map(x -> x.resolveBinding()
                .getType())
            .map(NameConverter::toPCMIdentifier)
            .collect(Collectors.toList());
        requiredInterfaces.get(unit)
            .addAll(ifaceNames);
        detectOperationInterface(field.getType());
        if (compositeName != null) {
            CompositeBuilder composite = getComposite(compositeName);
            for (String ifaceName : ifaceNames) {
                composite.addRequiredInterface(ifaceName);
            }
        }
    }

    public void detectRequiredInterface(CompilationUnit unit, SingleVariableDeclaration parameter) {
        detectRequiredInterface(unit, parameter, null);
    }

    private void detectRequiredInterface(CompilationUnit unit, SingleVariableDeclaration parameter,
            String compositeName) {
        if (requiredInterfaces.get(unit) == null) {
            requiredInterfaces.put(unit, new HashSet<>());
        }
        String ifaceName = NameConverter.toPCMIdentifier(parameter.resolveBinding()
            .getType());
        requiredInterfaces.get(unit)
            .add(ifaceName);
        detectOperationInterface(parameter.getType());
        if (compositeName != null) {
            getComposite(compositeName).addRequiredInterface(ifaceName);
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
        if (method == null) {
            LOG.warn("Unresolved method binding detected in " + getFullUnitName(unit) + "!");
            return;
        }
        if (providedInterfaces.get(unit) == null) {
            providedInterfaces.put(unit, new HashSet<>());
        }
        providedInterfaces.get(unit)
            .add(declaringIface);
    }

    public void detectPartOfComposite(CompilationUnit unit, String compositeName) {
        getComposite(compositeName).addPart(unit);
    }

    public void detectCompositeRequiredInterface(String compositeName, CompilationUnit unit, String interfaceName) {
        getComposite(compositeName).addRequiredInterface(interfaceName);
        detectRequiredInterface(unit, interfaceName);
    }

    public void detectCompositeRequiredInterface(String compositeName, CompilationUnit unit, FieldDeclaration field) {
        detectRequiredInterface(unit, field, compositeName);
    }

    public void detectCompositeRequiredInterface(String compositeName, CompilationUnit unit,
            SingleVariableDeclaration parameter) {
        detectRequiredInterface(unit, parameter, compositeName);
    }

    public void detectCompositeProvidedOperation(String compositeName, CompilationUnit unit, IMethodBinding method) {
        String declaringIface = NameConverter.toPCMIdentifier(method.getDeclaringClass());
        getComposite(compositeName).addProvidedOperation(declaringIface, method);
        detectProvidedOperation(unit, declaringIface, method);
    }

    public void detectCompositeProvidedOperation(String compositeName, CompilationUnit unit,
            ITypeBinding declaringIface, IMethodBinding method) {
        String declaringIfaceName = NameConverter.toPCMIdentifier(declaringIface);
        getComposite(compositeName).addProvidedOperation(declaringIfaceName, method);
        detectProvidedOperation(unit, declaringIfaceName, method);
    }

    public void detectCompositeProvidedOperation(String compositeName, CompilationUnit unit, String declaringIface,
            IMethodBinding method) {
        getComposite(compositeName).addProvidedOperation(declaringIface, method);
        detectProvidedOperation(unit, declaringIface, method);
    }

    private CompositeBuilder getComposite(String name) {
        if (!composites.containsKey(name)) {
            composites.put(name, new CompositeBuilder());
        }
        return composites.get(name);
    }

    @Override
    public List<CompilationUnitWrapper> getWrappedComponents() {
        return CompilationUnitWrapper.wrap(components);
    }

    protected List<CompilationUnit> getComponents() {
        return components;
    }

    protected Set<String> getProvidedInterfaces(CompilationUnit unit) {
        final Set<String> set = providedInterfaces.get(unit);
        if (set == null) {
            return new HashSet<>();
        }
        return Collections.unmodifiableSet(set);
    }

    protected Set<String> getRequiredInterfaces(CompilationUnit unit) {
        final Set<String> set = requiredInterfaces.get(unit);
        if (set == null) {
            return new HashSet<>();
        }
        return Collections.unmodifiableSet(set);
    }

    protected Map<String, List<IMethodBinding>> getOperationInterfaces() {
        return Collections.unmodifiableMap(operationInterfaces);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(149);
        sb.append("[PCMDetectorSimple] {\n\tcomponents: {\n");
        components.forEach(comp -> {
            sb.append("\t\t")
                .append(getFullUnitName(comp))
                .append('\n');
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

        sb.append("\t}\n\tprovided relations: {\n")
            .append(mapToString(providedInterfaces, 2))
            .append("\t}\n\trequired interfaces: {\n")
            .append(mapToString(requiredInterfaces, 2))
            .append("\t}\n}");
        return sb.toString();
    }

    private static String mapToString(Map<?, ? extends Collection<?>> map, int indentation) {
        StringBuilder sb = new StringBuilder();
        String indentString = "\t".repeat(indentation);
        map.entrySet()
            .forEach(entry -> {
                sb.append(indentString);
                sb.append('\"');
                sb.append(entry.getKey());
                sb.append("\" -> {");
                entry.getValue()
                    .forEach(value -> {
                        sb.append("\t".repeat(indentation + 1));
                        sb.append(value);
                        sb.append('\n');
                    });
                sb.append(indentString);
                sb.append("}\n");
            });
        return sb.toString();
    }

}
