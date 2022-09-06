package org.palladiosimulator.somox.analyzer.rules.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.emftext.language.java.classifiers.Class;
import org.emftext.language.java.classifiers.Classifier;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.classifiers.Interface;
import org.emftext.language.java.containers.impl.CompilationUnitImpl;
import org.emftext.language.java.members.Method;
import org.emftext.language.java.variables.Variable;
import org.palladiosimulator.somox.analyzer.rules.blackboard.CompilationUnitWrapper;

/**
 * This class is used to detect and hold all relevant elements found during the processing of rules.
 * It provides methods to detect and retrieve PCM elements. After all rules are parsed, this class
 * holds the results as "simple" java objects not yet transformed to real PCM objects like PCM Basic
 * Components.
 */
public class EMFTextPCMDetector implements IPCMDetector {
    private final List<CompilationUnitImpl> components = new ArrayList<>();

    private final Map<String, List<EMFTextProvidesRelation>> providedRelations = new HashMap<>();

    private final Map<String, List<Variable>> requiredInterfaces = new HashMap<>();

    private final List<Classifier> operationInterfaces = new ArrayList<>();

    private final Set<String> interfaceNames = new HashSet<>();

    private static String getFullUnitName(CompilationUnitImpl unit) {
        return unit.getNamespacesAsString() + "." + unit.getName();
    }

    public void detectComponent(CompilationUnitImpl unit) {
        for (final ConcreteClassifier classi : unit.getClassifiers()) {
            if ((classi instanceof Class) || (classi instanceof Interface)) {
                components.add(unit);
            }
        }
    }

    public void detectOperationInterface(CompilationUnitImpl unit) {
        for (final ConcreteClassifier classi : unit.getClassifiers()) {
            detectOperationInterface(classi);
        }

    }

    private void detectOperationInterface(Classifier classifier) {
        List<String> names = new LinkedList<>(classifier.getContainingPackageName());
        names.add(classifier.getName());
        String name = String.join(".", names);

        if (interfaceNames.contains(name)) {
            return;
        }
        if ((classifier instanceof Class) || (classifier instanceof Interface)) {
            operationInterfaces.add(classifier);
            interfaceNames.add(name);
        }
    }

    public void detectOperationInterface(Interface in) {
        detectOperationInterface((Classifier) in);
    }

    public void detectRequiredInterface(CompilationUnitImpl unit, Variable v) {
        final String unitName = getFullUnitName(unit);
        if (requiredInterfaces.get(unitName) == null) {
            final List<Variable> fields = new ArrayList<>();
            requiredInterfaces.put(unitName, fields);
        }
        requiredInterfaces.get(unitName)
            .add(v);
        Classifier currentClassi = v.getTypeReference()
            .getPureClassifierReference()
            .getTarget();
        detectOperationInterface(currentClassi);

    }

    public void detectProvidedInterface(CompilationUnitImpl unit, Method method) {
        detectProvidedInterface(unit, unit.getClassifiers()
            .get(0), method);
    }

    public void detectProvidedInterface(CompilationUnitImpl unit, Classifier opI, Method method) {
        final String unitName = getFullUnitName(unit);
        final EMFTextProvidesRelation relation = new EMFTextProvidesRelation(opI, method);
        if (providedRelations.get(unitName) == null) {
            providedRelations.put(unitName, new ArrayList<>());
        }
        providedRelations.get(unitName)
            .add(relation);

    }

    @Override
    public List<CompilationUnitWrapper> getWrappedComponents() {
        return CompilationUnitWrapper.wrap(components);
    }

    protected List<CompilationUnitImpl> getComponents() {
        return components;
    }

    protected List<EMFTextProvidesRelation> getProvidedInterfaces(CompilationUnitImpl unit) {
        final String name = getFullUnitName(unit);
        if (providedRelations.get(name) == null) {
            return new ArrayList<>();
        }
        return providedRelations.get(name);
    }

    protected List<Variable> getRequiredInterfaces(CompilationUnitImpl unit) {
        final String name = getFullUnitName(unit);
        if (requiredInterfaces.get(name) == null) {
            return new ArrayList<>();
        }
        return requiredInterfaces.get(name);
    }

    protected List<Classifier> getOperationInterfaces() {
        return operationInterfaces.stream()
            .distinct()
            .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(140);
        sb.append("[PCMDetectorSimple] {\n\tcomponents: {\n");
        components.forEach(comp -> {
            sb.append("\t\t");
            sb.append(comp.getNamespacesAsString());
            sb.append('.');
            sb.append(comp.getName());
            sb.append('\n');
        });

        sb.append("\t}\n\tinterfaces: {\n");
        operationInterfaces.forEach(op -> {
            sb.append("\t\t");
            sb.append(op.getName());
            sb.append('\n');
        });

        sb.append("\t}\n\tprovided relations: {\n")
            .append(mapToString(providedRelations, 2))
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