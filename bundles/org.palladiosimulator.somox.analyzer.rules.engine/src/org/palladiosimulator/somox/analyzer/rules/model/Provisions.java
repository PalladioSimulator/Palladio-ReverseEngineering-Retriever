package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Provisions implements Iterable<Provision> {
    private final Set<Provision> provisions;

    public Provisions(Collection<Provision> provisions) {
        // TODO: Grouping algorithm - longest common prefix etc.,
        // probably requires more functionality in Provision

        this.provisions = Collections.unmodifiableSet(new HashSet<>(provisions));
    }

    public Set<Provision> get() {
        return provisions;
    }

    public boolean contains(String iface) {
        return provisions.stream()
            .anyMatch(x -> x.isPartOf(iface));
    }

    public boolean contains(Operation operation) {
        return provisions.contains(operation);
    }

    @Override
    public Iterator<Provision> iterator() {
        return provisions.iterator();
    }
}
