package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.Optional;

public interface OperationName {
    String getFullName();

    Optional<String> getName(String baseInterface);

    boolean isPartOf(String iface);
}