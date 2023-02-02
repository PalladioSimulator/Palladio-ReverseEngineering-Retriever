package org.palladiosimulator.somox.analyzer.rules.engine;

import java.util.Collections;
import java.util.Set;

import org.palladiosimulator.somox.analyzer.rules.model.Component;
import org.palladiosimulator.somox.analyzer.rules.model.Provisions;
import org.palladiosimulator.somox.analyzer.rules.model.Requirements;

public class Composite {

    private final String name;
    private final Set<Component> parts;
    private final Set<String> internalInterfaces;
    private final Requirements requirements;
    private final Provisions provisions;

    public Composite(String name, Set<Component> parts, Requirements requirements, Provisions provisions,
            Set<String> internalInterfaces) {
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

    public Set<String> internalInterfaces() {
        return Collections.unmodifiableSet(internalInterfaces);
    }

    public boolean isSubsetOf(final Composite other) {
        return other.parts()
            .containsAll(parts);
    }
}
