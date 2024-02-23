package org.palladiosimulator.retriever.extraction.commonalities;

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
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.palladiosimulator.retriever.extraction.engine.MapMerger;

public class Provisions implements Iterable<OperationInterface> {
    private final Set<OperationInterface> provisions;
    private final Map<OperationInterface, List<OperationInterface>> groupedProvisions;

    public Provisions(final Collection<OperationInterface> provisions,
            final Collection<OperationInterface> allDependencies) {
        this.provisions = Collections.unmodifiableSet(new HashSet<>(provisions));
        this.groupedProvisions = DependencyUtils.groupDependencies(provisions, allDependencies);
    }

    public Set<OperationInterface> get() {
        return this.provisions;
    }

    public Map<OperationInterface, List<OperationInterface>> getGrouped() {
        return this.groupedProvisions;
    }

    public boolean containsPartOf(final OperationInterface iface) {
        return this.provisions.stream()
            .anyMatch(x -> x.isPartOf(iface));
    }

    public boolean containsEntire(final OperationInterface iface) {
        return this.provisions.stream()
            .anyMatch(x -> iface.isPartOf(x));
    }

    @Override
    public Iterator<OperationInterface> iterator() {
        return this.provisions.iterator();
    }

    public Map<OperationInterface, SortedSet<Operation>> simplified() {
        final List<Map<OperationInterface, SortedSet<Operation>>> simplifiedInterfaces = new LinkedList<>();
        for (final OperationInterface root : this.groupedProvisions.keySet()) {
            final List<Operation> simplifiedRoot = new ArrayList<>(root.simplified()
                .values()
                .stream()
                .flatMap(x -> x.stream())
                .collect(Collectors.toList()));
            for (final OperationInterface member : this.groupedProvisions.get(root)) {
                simplifiedRoot.addAll(member.simplified()
                    .values()
                    .stream()
                    .flatMap(x -> x.stream())
                    .collect(Collectors.toList()));
            }
            simplifiedInterfaces.add(Map.of(root, simplifiedRoot.stream()
                .distinct()
                .collect(Collectors.toCollection(TreeSet::new))));
        }
        return MapMerger.merge(simplifiedInterfaces);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.provisions);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (this.getClass() != obj.getClass())) {
            return false;
        }
        final Provisions other = (Provisions) obj;
        return Objects.equals(this.provisions, other.provisions);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        final Map<OperationInterface, SortedSet<Operation>> simplified = this.simplified();

        for (final OperationInterface iface : simplified.keySet()) {
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
