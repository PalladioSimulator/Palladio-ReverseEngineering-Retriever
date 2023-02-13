package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.Optional;

public interface OperationName extends Name {
    Optional<String> forInterface(String baseInterface);

    // Returns the most specific entire interface.
    String getInterface();
}