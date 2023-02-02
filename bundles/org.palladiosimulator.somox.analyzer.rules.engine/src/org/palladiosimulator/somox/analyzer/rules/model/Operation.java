package org.palladiosimulator.somox.analyzer.rules.model;

import org.eclipse.jdt.core.dom.IMethodBinding;

public class Operation implements Provision {
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
    public boolean isPartOf(String baseInterface) {
        return name.isPartOf(baseInterface);
    }

}
