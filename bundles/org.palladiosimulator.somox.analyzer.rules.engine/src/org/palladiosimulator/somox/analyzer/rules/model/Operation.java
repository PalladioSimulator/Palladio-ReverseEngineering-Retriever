package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.core.dom.IMethodBinding;

public class Operation implements OperationInterface {
    private final IMethodBinding binding;
    private final OperationName name;

    public Operation(IMethodBinding binding, OperationName name) {
        this.binding = binding;
        this.name = name;
    }

    public IMethodBinding getBinding() {
        return binding;
    }

    public OperationName getName() {
        return name;
    }

    @Override
    public String getInterface() {
        return name.getInterface();
    }

    @Override
    public boolean isPartOf(OperationInterface other) {
        if (other.isEntireInterface()) {
            return name.isPartOf(other.getInterface());
        } else {
            return this.equals(other);
        }
    }

    @Override
    public boolean isEntireInterface() {
        return false;
    }

    @Override
    public Map<String, List<Operation>> simplified() {
        return Map.of(getInterface(), List.of(this));
    }

    @Override
    public int hashCode() {
        return Objects.hash(binding, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Operation other = (Operation) obj;
        return Objects.equals(binding, other.binding) && Objects.equals(name, other.name);
    }

}
