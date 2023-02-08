package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ITypeBinding;

public class EntireInterface implements OperationInterface {
    private final Optional<ITypeBinding> binding;
    private final InterfaceName name;

    public EntireInterface(InterfaceName name) {
        this.binding = Optional.empty();
        this.name = name;
    }

    public EntireInterface(ITypeBinding binding, InterfaceName name) {
        this.binding = Optional.of(binding);
        this.name = name;
    }

    public Optional<ITypeBinding> getBinding() {
        return binding;
    }

    public Name getName() {
        return name;
    }

    @Override
    public Map<String, List<Operation>> simplified() {
        return Map.of(name.getName(), List.of());
    }

    @Override
    public String getInterface() {
        return name.getInterfaces()
            .get(0);
    }
}
