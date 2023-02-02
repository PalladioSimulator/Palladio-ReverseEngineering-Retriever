package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Requirements implements Iterable<String> {
    private final Set<String> requiredInterfaces;

    public Requirements(Collection<String> requiredInterfaces) {
        this.requiredInterfaces = new HashSet<>(requiredInterfaces);
    }

    public Set<String> get() {
        return Collections.unmodifiableSet(requiredInterfaces);
    }

    public boolean contains(String iface) {
        return requiredInterfaces.contains(iface);
    }

    // This only checks whether a required interface could contain {@code operation},
    // not whether the operation is specifically required.
    public boolean contains(Operation operation) {
        return requiredInterfaces.stream()
            .anyMatch(x -> operation.getName()
                .isPartOf(x));
    }

    @Override
    public Iterator<String> iterator() {
        return Collections.unmodifiableCollection(requiredInterfaces)
            .iterator();
    }
}
