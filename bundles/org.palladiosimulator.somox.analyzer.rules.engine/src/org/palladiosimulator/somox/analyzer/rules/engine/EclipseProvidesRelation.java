package org.palladiosimulator.somox.analyzer.rules.engine;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

// Encapsulates the operation interface and method a component is providing.
// This class is required because a method from the java model itself does not contain a reference to the interface
// it comes from like variables do.
public class EclipseProvidesRelation {
    private final ITypeBinding operationInterface;
    private final IMethodBinding method;

    public EclipseProvidesRelation(ITypeBinding opI, IMethodBinding method) {
        operationInterface = opI;
        this.method = method;
    }

    @Override
    public String toString() {
        return operationInterface.getQualifiedName() + ": " + method.toString();
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

    public ITypeBinding getOperationInterface() {
        return operationInterface;
    }

    public IMethodBinding getMethod() {
        return method;
    }

}
