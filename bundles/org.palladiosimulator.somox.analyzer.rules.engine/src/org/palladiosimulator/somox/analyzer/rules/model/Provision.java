package org.palladiosimulator.somox.analyzer.rules.model;

public interface Provision {
    String getInterface();

    boolean isPartOf(String baseInterface);
}
