package org.palladiosimulator.somox.analyzer.rules.engine;

import java.util.stream.Collectors;

import org.emftext.language.java.classifiers.Classifier;
import org.emftext.language.java.members.Method;
import org.emftext.language.java.parameters.Parameter;

// Encapsulates the operation interface and method a component is providing.
// This class is required because a method from the java model itself does not contain a reference to the interface
// it comes from like variables do.
public class EMFTextProvidesRelation {
    private final Classifier operationInterface;
    private final Method method;

    public EMFTextProvidesRelation(Classifier operationInterface, Method method) {
        this.operationInterface = operationInterface;
        this.method = method;
    }

    @Override
    public String toString() {
        String parameterString = "";
        parameterString += method.getParameters()
            .stream()
            .map(Parameter::getName)
            .collect(Collectors.joining(","));
        return operationInterface.getName() + ": " + method.getName() + "(" + parameterString + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        return (prime * result) + toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        return toString().equals(obj.toString());
    }

    public Classifier getOperationInterface() {
        return operationInterface;
    }

    public Method getMethod() {
        return method;
    }

}
