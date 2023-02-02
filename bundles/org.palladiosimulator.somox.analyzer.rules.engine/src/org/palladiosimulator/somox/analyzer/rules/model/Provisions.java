package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Provisions {
    private final Set<Operation> provisions;

    public Provisions(Collection<Operation> provisions) {
        this.provisions = new HashSet<>(provisions);
    }

    public Set<Operation> get() {
        return Collections.unmodifiableSet(provisions);
    }

    public boolean contains(String iface) {
        return provisions.stream()
            .anyMatch(x -> x.getName()
                .isPartOf(iface));
    }

    public boolean contains(Operation operation) {
        return provisions.contains(operation);
    }
}
