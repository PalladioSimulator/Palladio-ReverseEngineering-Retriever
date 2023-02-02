package org.palladiosimulator.somox.analyzer.rules.model;

import org.eclipse.jdt.core.dom.IMethodBinding;

public class Operation {
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
}
