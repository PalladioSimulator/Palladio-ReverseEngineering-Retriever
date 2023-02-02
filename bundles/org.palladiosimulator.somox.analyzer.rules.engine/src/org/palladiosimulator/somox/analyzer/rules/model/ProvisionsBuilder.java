package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ProvisionsBuilder {
    private final List<Operation> providedOperations = new LinkedList<>();

    public void add(Operation... operations) {
        this.add(Set.of(operations));
    }

    public void add(Collection<Operation> operations) {
        providedOperations.addAll(operations);
    }

    public Provisions create() {
        return new Provisions(providedOperations);
    }
}
