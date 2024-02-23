package org.palladiosimulator.retriever.extraction.commonalities;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jdt.core.dom.ITypeBinding;

public class EntireInterface implements OperationInterface {
    private final Optional<ITypeBinding> binding;
    private final InterfaceName name;

    public EntireInterface(final InterfaceName name) {
        this.binding = Optional.empty();
        this.name = name;
    }

    public EntireInterface(final ITypeBinding binding, final InterfaceName name) {
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
    public Map<OperationInterface, SortedSet<Operation>> simplified() {
        return Map.of(this, new TreeSet<>());
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
