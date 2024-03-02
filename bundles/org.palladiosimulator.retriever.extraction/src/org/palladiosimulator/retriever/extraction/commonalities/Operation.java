package org.palladiosimulator.retriever.extraction.commonalities;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.core.dom.IMethodBinding;

public class Operation implements OperationInterface {
    private final IMethodBinding binding;
    private final OperationName name;

    public Operation(final IMethodBinding binding, final OperationName name) {
        this.binding = binding;
        this.name = name;
    }

    public IMethodBinding getBinding() {
        return this.binding;
    }

    @Override
    public OperationName getName() {
        return this.name;
    }

    @Override
    public boolean isPartOf(final OperationInterface other) {
        if (other instanceof Operation otherOperation) {
            return Objects.equals(this.binding, otherOperation.binding);
        } else {
            return OperationInterface.super.isPartOf(other);
        }
    }

    @Override
    public Map<OperationInterface, Set<Operation>> simplified() {
        return Map.of(this, Set.of(this));
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.binding, this.name);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (this.getClass() != obj.getClass())) {
            return false;
        }
        final Operation other = (Operation) obj;
        return Objects.equals(this.binding, other.binding) && Objects.equals(this.name, other.name);
    }

    @Override
    public String getInterface() {
        return this.name.getInterface();
    }

    @Override
    public String toString() {
        return this.getName()
            .toString();
    }
}
