package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.Optional;

public interface OperationName {
    String getFullName();

    Optional<String> forInterface(String baseInterface);

    String getInterface();

    boolean isPartOf(String iface);
}