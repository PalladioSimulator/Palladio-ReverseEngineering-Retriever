package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.IMethodBinding;

public interface OperationInterface {
    String getInterface();

    boolean isPartOf(OperationInterface other);

    boolean isEntireInterface();

    Map<String, List<IMethodBinding>> simplified();
}
