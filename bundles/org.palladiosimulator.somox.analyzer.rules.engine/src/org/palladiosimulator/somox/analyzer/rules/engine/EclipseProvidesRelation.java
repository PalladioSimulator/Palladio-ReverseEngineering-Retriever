package org.palladiosimulator.somox.analyzer.rules.engine;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

// Encapsulates the operation interface and method a component is providing.
// This class is required because a method from the java model itself does not contain a reference to the interface it comes from like variables do.
public class EclipseProvidesRelation {
    private final ITypeBinding operationInterface;
    private final IMethodBinding method;

    public EclipseProvidesRelation(ITypeBinding opI, IMethodBinding method) {
        super();
        this.operationInterface = opI;
        this.method = method;
    }

    @Override
    public String toString() {
        return (operationInterface.getQualifiedName() + ": " + method.toString());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (toString().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EclipseProvidesRelation other = (EclipseProvidesRelation) obj;
        if (!toString().equals(other.toString())) {
            return false;
        }
        return true;
    }

    public ITypeBinding getOperationInterface() {
        return operationInterface;
    }

    public IMethodBinding getMethod() {
        return method;
    }

}
