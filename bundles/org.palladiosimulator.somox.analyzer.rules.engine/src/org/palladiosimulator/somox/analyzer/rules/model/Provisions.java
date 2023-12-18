package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.palladiosimulator.somox.analyzer.rules.engine.MapMerger;

public class Provisions implements Iterable<OperationInterface> {
    private final Set<OperationInterface> provisions;
    private final Map<OperationInterface, List<OperationInterface>> groupedProvisions;

    public Provisions(Collection<OperationInterface> provisions, Collection<OperationInterface> allDependencies) {
        this.provisions = Collections.unmodifiableSet(new HashSet<>(provisions));
        this.groupedProvisions = DependencyUtils.groupDependencies(provisions, allDependencies);
    }

    public Set<OperationInterface> get() {
        return provisions;
    }

    public Map<OperationInterface, List<OperationInterface>> getGrouped() {
        return groupedProvisions;
    }

    public boolean containsPartOf(OperationInterface iface) {
        return provisions.stream()
            .anyMatch(x -> x.isPartOf(iface));
    }

    public boolean containsEntire(OperationInterface iface) {
        return provisions.stream()
            .anyMatch(x -> iface.isPartOf(x));
    }

    @Override
    public Iterator<OperationInterface> iterator() {
        return provisions.iterator();
    }

    public Map<OperationInterface, List<Operation>> simplified() {
        List<Map<OperationInterface, List<Operation>>> simplifiedInterfaces = new LinkedList<>();
        for (OperationInterface root : groupedProvisions.keySet()) {
            List<Operation> simplifiedRoot = new ArrayList<>(root.simplified()
                .values()
                .stream()
                .flatMap(x -> x.stream())
                .collect(Collectors.toList()));
            for (OperationInterface member : groupedProvisions.get(root)) {
                simplifiedRoot.addAll(member.simplified()
                    .values()
                    .stream()
                    .flatMap(x -> x.stream())
                    .collect(Collectors.toList()));
            }
            simplifiedInterfaces.add(Map.of(root, simplifiedRoot.stream()
                .distinct()
                .collect(Collectors.toList())));
        }
        return MapMerger.merge(simplifiedInterfaces);
    }

    @Override
    public int hashCode() {
        return Objects.hash(provisions);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Provisions other = (Provisions) obj;
        return Objects.equals(provisions, other.provisions);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Map<OperationInterface, List<Operation>> simplified = simplified();

        for (OperationInterface iface : simplified.keySet()) {
            builder.append(iface.getName());
            simplified.get(iface)
                .forEach(x -> builder.append("\n\t")
                    .append(x));
            builder.append('\n');
        }

        String result = builder.toString();
        if (result.endsWith("\n")) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }
}
