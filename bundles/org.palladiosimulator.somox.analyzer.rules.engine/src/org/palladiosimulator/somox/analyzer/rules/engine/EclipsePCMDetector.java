package org.palladiosimulator.somox.analyzer.rules.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private Map<String, List<EclipseProvidesRelation>> providedRelations = new HashMap<>();

    // TODO Is this a good solution or would a union be better?
    private Map<String, List<List<VariableDeclaration>>> requiredInterfaces = new HashMap<>();

    private Set<ITypeBinding> operationInterfaces = new HashSet<>();

    private String getFullUnitName(CompilationUnit unit) {
        // TODO this is potentially problematic, maybe restructure
        // On the other hand, it is still fit as a unique identifier,
        // since types cannot be declared multiple times.

        List<String> names = getFullUnitNames(unit);
        if (!names.isEmpty()) {
            return names.get(0);
        } else {
            return null;
        }
    }

    private List<String> getFullUnitNames(CompilationUnit unit) {
        List<String> names = new ArrayList<String>();
        for (Object type : unit.types()) {
            if (type instanceof AbstractTypeDeclaration) {
                names.add(getFullTypeName((AbstractTypeDeclaration) type));
            }
        }

        return names;
    }

    private String getFullTypeName(AbstractTypeDeclaration type) {
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

    private void detectOperationInterface(AbstractTypeDeclaration type) {
        if (type instanceof TypeDeclaration) {
            ITypeBinding binding = type.resolveBinding();
            if (binding == null) {
                LOG.warn("Unresolved interface binding detected in " + getFullTypeName(type) + "!");
                return;
            }
            operationInterfaces.add(binding.getTypeDeclaration());
        }
    }

    public void detectOperationInterface(Type type) {
        ITypeBinding binding = type.resolveBinding();
        if (binding == null) {
            LOG.warn("Unresolved interface binding detected!");
            return;
        }
        ITypeBinding typeBinding = binding.getTypeDeclaration();
        if (typeBinding == null) {
            LOG.warn("Unresolved interface binding detected!");
            return;
        }
        if (typeBinding.isClass() || typeBinding.isInterface()) {
            operationInterfaces.add(typeBinding);
        }
    }

    public void detectRequiredInterface(CompilationUnit unit, FieldDeclaration field) {
        final String unitName = getFullUnitName(unit);
        if (requiredInterfaces.get(unitName) == null) {
            requiredInterfaces.put(unitName, new ArrayList<>());
        }
        @SuppressWarnings("unchecked")
        List<VariableDeclaration> fragments = (List<VariableDeclaration>) field.fragments();
        requiredInterfaces.get(unitName)
            .add(fragments);
        detectOperationInterface(field.getType());

    }

    public void detectRequiredInterface(CompilationUnit unit, SingleVariableDeclaration parameter) {
        final String unitName = getFullUnitName(unit);
        if (requiredInterfaces.get(unitName) == null) {
            requiredInterfaces.put(unitName, new ArrayList<>());
        }
        requiredInterfaces.get(unitName)
            .add(List.of(parameter));
        detectOperationInterface(parameter.getType());

    }

    public void detectProvidedInterface(CompilationUnit unit, IMethodBinding method) {
        if (method == null) {
            LOG.warn("Unresolved method binding detected in " + getFullUnitName(unit) + "!");
            return;
        }
        detectProvidedInterface(unit, method.getDeclaringClass(), method);
    }

    public void detectProvidedInterface(CompilationUnit unit, ITypeBinding opI, IMethodBinding method) {
        if (opI == null) {
            LOG.warn("Unresolved type binding detected in " + getFullUnitName(unit) + "!");
            return;
        }
        if (method == null) {
            LOG.warn("Unresolved method binding detected in " + getFullUnitName(unit) + "!");
            return;
        }
        final String unitName = getFullUnitName(unit);
        if (providedRelations.get(unitName) == null) {
            providedRelations.put(unitName, new ArrayList<>());
        }
        providedRelations.get(unitName)
            .add(new EclipseProvidesRelation(opI, method));
    }

    @Override
    public List<CompilationUnitWrapper> getWrappedComponents() {
        return CompilationUnitWrapper.wrap(components);
    }

    protected List<CompilationUnit> getComponents() {
        return components;
    }

    protected List<EclipseProvidesRelation> getProvidedInterfaces(CompilationUnit unit) {
        final String name = getFullUnitName(unit);
        if (providedRelations.get(name) == null) {
            return new ArrayList<>();
        }
        return providedRelations.get(name);
    }

    protected List<List<VariableDeclaration>> getRequiredInterfaces(CompilationUnit unit) {
        final String name = getFullUnitName(unit);
        if (requiredInterfaces.get(name) == null) {
            return new ArrayList<>();
        }
        return requiredInterfaces.get(name);
    }

    protected List<ITypeBinding> getOperationInterfaces() {
        return List.of(operationInterfaces.toArray(new ITypeBinding[0]));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[PCMDetectorSimple] {\n");

        sb.append("\tcomponents: {\n");
        components.forEach(comp -> {
            sb.append("\t\t");
            sb.append(getFullUnitName(comp));
            sb.append("\n");
        });

        sb.append("\t}\n\tinterfaces: {\n");
        operationInterfaces.forEach(op -> {
            sb.append("\t\t");
            sb.append(op.getQualifiedName());
            sb.append("\n");
        });

        sb.append("\t}\n\tprovided relations: {\n");
        sb.append(mapToString(providedRelations, 2));
        sb.append("\t}\n\trequired interfaces: {\n");
        sb.append(mapToString(requiredInterfaces, 2));
        sb.append("\t}\n}");
        return sb.toString();
    }

    private String mapToString(Map<?, ? extends Collection<?>> map, int indentation) {
        StringBuilder sb = new StringBuilder();
        requiredInterfaces.entrySet()
            .forEach(entry -> {
                sb.append("\t".repeat(indentation));
                sb.append("\"");
                sb.append(entry.getKey());
                sb.append("\" -> {");
                entry.getValue()
                    .forEach(value -> {
                        sb.append("\t".repeat(indentation + 1));
                        sb.append(value);
                        sb.append("\n");
                    });
                sb.append("\t".repeat(indentation));
                sb.append("}\n");
            });
        return sb.toString();
    }

}