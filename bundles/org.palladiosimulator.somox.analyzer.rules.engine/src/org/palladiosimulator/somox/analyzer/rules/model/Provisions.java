package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.palladiosimulator.somox.analyzer.rules.engine.MapMerger;

public class Provisions implements Iterable<OperationInterface> {
    private final Set<OperationInterface> provisions;

    public Provisions(Collection<OperationInterface> provisions) {
        // TODO: Grouping algorithm - longest common prefix etc.,
        // probably requires more functionality in Provision

        this.provisions = Collections.unmodifiableSet(new HashSet<>(provisions));
    }

    public Set<OperationInterface> get() {
        return provisions;
    }

    public boolean contains(OperationInterface iface) {
        return provisions.stream()
            .anyMatch(x -> x.isPartOf(iface));
    }

    @Override
    public Iterator<OperationInterface> iterator() {
        return provisions.iterator();
    }

    public Map<String, List<Operation>> simplified() {
        List<Map<String, List<Operation>>> simplifiedInterfaces = provisions.stream()
            .map(OperationInterface::simplified)
            .collect(Collectors.toList());

        return MapMerger.merge(simplifiedInterfaces);
    }
}
