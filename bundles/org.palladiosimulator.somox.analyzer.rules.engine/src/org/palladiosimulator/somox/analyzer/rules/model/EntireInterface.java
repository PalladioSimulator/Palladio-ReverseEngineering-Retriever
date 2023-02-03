package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

// TODO: Handle paths properly
public class EntireInterface implements OperationInterface {
    private final Optional<ITypeBinding> binding;
    private final String name;

    public EntireInterface(String name) {
        this.binding = Optional.empty();
        this.name = name;
    }

    public EntireInterface(ITypeBinding binding, String name) {
        this.binding = Optional.of(binding);
        this.name = name;
    }

    public Optional<ITypeBinding> getBinding() {
        return binding;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getInterface() {
        return getName();
    }

    @Override
    public boolean isPartOf(OperationInterface other) {
        return name.equals(other.getInterface());
    }

    @Override
    public boolean isEntireInterface() {
        return true;
    }

    @Override
    public Map<String, List<IMethodBinding>> simplified() {
        return Map.of(name, List.of());
    }
}
