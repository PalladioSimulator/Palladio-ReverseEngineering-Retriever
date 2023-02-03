package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.Collections;
import java.util.Set;

public class Composite {

    private final String name;
    private final Set<Component> parts;
    private final Set<OperationInterface> internalInterfaces;
    private final Requirements requirements;
    private final Provisions provisions;

    public Composite(String name, Set<Component> parts, Requirements requirements, Provisions provisions,
            Set<OperationInterface> internalInterfaces) {
        this.name = name;
        this.parts = parts;
        this.internalInterfaces = internalInterfaces;
        this.requirements = requirements;
        this.provisions = provisions;
    }

    public String name() {
        return name;
    }

    public Requirements requirements() {
        return requirements;
    }

    public Provisions provisions() {
        return provisions;
    }

    public Set<Component> parts() {
        return Collections.unmodifiableSet(parts);
    }

    public Set<OperationInterface> internalInterfaces() {
        return Collections.unmodifiableSet(internalInterfaces);
    }

    public boolean isSubsetOf(final Composite other) {
        return other.parts()
            .containsAll(parts);
    }
}
