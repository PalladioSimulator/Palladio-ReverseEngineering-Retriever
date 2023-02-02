package org.palladiosimulator.somox.analyzer.rules.engine;

import java.util.Collections;
import java.util.Set;

import org.palladiosimulator.somox.analyzer.rules.model.Component;
import org.palladiosimulator.somox.analyzer.rules.model.Operation;

public class Composite {

    private String name;
    private Set<Component> parts;
    private Set<String> internalInterfaces;
    private Set<String> requiredInterfaces;
    private Set<Operation> providedOperations;

    public Composite(String name, Set<Component> parts, Set<String> requiredInterfaces,
            Set<Operation> providedOperations, Set<String> internalInterfaces) {
        this.name = name;
        this.parts = parts;
        this.internalInterfaces = internalInterfaces;
        this.requiredInterfaces = requiredInterfaces;
        this.providedOperations = providedOperations;
    }

    public String getName() {
        return name;
    }

    public Set<Component> getParts() {
        return Collections.unmodifiableSet(parts);
    }

    public Set<String> getInternalInterfaces() {
        return Collections.unmodifiableSet(internalInterfaces);
    }

    public Set<String> getRequiredInterfaces() {
        return Collections.unmodifiableSet(requiredInterfaces);
    }

    public Set<Operation> getProvidedInterfaces() {
        return Collections.unmodifiableSet(providedOperations);
    }

    public boolean isSubsetOf(final Composite other) {
        return other.getParts()
            .containsAll(parts);
    }
}
