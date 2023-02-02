package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Provisions implements Iterable<Operation> {
    private final Set<Operation> providedOperations;

    public Provisions(Collection<Operation> providedOperations) {
        this.providedOperations = new HashSet<>(providedOperations);
    }

    public Set<Operation> get() {
        return Collections.unmodifiableSet(providedOperations);
    }

    public boolean contains(String iface) {
        return providedOperations.stream()
            .anyMatch(x -> x.getName()
                .isPartOf(iface));
    }

    public boolean contains(Operation operation) {
        return providedOperations.contains(operation);
    }

    @Override
    public Iterator<Operation> iterator() {
        return Collections.unmodifiableCollection(providedOperations)
            .iterator();
    }
}
