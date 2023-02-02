package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class RequirementsBuilder {
    private final List<String> requiredInterfaces = new LinkedList<>();

    public void add(String... interfaces) {
        this.add(interfaces);
    }

    public void add(Collection<String> interfaces) {
        requiredInterfaces.addAll(interfaces);
    }

    public Requirements create() {
        return new Requirements(requiredInterfaces);
    }
}
