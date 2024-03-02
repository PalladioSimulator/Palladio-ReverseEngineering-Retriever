package org.palladiosimulator.retriever.extraction.commonalities;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.core.dom.ITypeBinding;

public class EntireInterface implements OperationInterface {
    private final Optional<ITypeBinding> binding;
    private final Name name;

    public EntireInterface(final Name name) {
        this.binding = Optional.empty();
        this.name = name;
    }

    public EntireInterface(final ITypeBinding binding, final Name name) {
        this.binding = Optional.of(binding);
        this.name = name;
    }

    public Optional<ITypeBinding> getBinding() {
        return this.binding;
    }

    @Override
    public Name getName() {
        return this.name;
    }

    @Override
    public Map<OperationInterface, Set<Operation>> simplified() {
        return Map.of(this, new HashSet<>());
    }

    @Override
    public String getInterface() {
        return this.name.getInterfaces()
            .get(0);
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
        final EntireInterface other = (EntireInterface) obj;
        return Objects.equals(this.binding, other.binding) && Objects.equals(this.name, other.name);
    }

    @Override
    public String toString() {
        return this.name.toString();
    }
}
