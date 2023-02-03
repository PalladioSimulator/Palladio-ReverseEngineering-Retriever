package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class RequirementsBuilder {
    private final List<EntireInterface> requirements = new LinkedList<>();

    public void add(EntireInterface... interfaces) {
        this.add(List.of(interfaces));
    }

    public void add(Collection<EntireInterface> interfaces) {
        requirements.addAll(interfaces);
    }

    public Requirements create() {
        return new Requirements(requirements);
    }
}
