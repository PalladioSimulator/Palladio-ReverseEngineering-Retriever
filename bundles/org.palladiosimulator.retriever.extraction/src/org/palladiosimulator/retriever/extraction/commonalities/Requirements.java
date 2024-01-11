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
import java.util.stream.Collectors;

import org.palladiosimulator.retriever.extraction.engine.MapMerger;

public class Requirements implements Iterable<OperationInterface> {
    private final Set<OperationInterface> requirements;
    private final Map<OperationInterface, List<OperationInterface>> groupedRequirements;

    public Requirements(Collection<OperationInterface> requiredInterfaces,
            Collection<OperationInterface> allDependencies, Collection<OperationInterface> visibleProvisions) {
        this.requirements = new HashSet<>();

        List<OperationInterface> sortedProvisions = new ArrayList<>(visibleProvisions);
        Collections.sort(sortedProvisions);
        Collections.reverse(sortedProvisions);
        for (OperationInterface requirement : requiredInterfaces) {
            OperationInterface generalizedRequirement = requirement;
            for (OperationInterface provision : sortedProvisions) {
                if (requirement.isPartOf(provision)) {
                    generalizedRequirement = provision;
                    break;
                }
            }
            requirements.add(generalizedRequirement);
        }

        this.groupedRequirements = DependencyUtils.groupDependencies(requirements, allDependencies);
    }

    public Set<OperationInterface> get() {
        return Collections.unmodifiableSet(requirements);
    }

    public boolean containsPartOf(OperationInterface iface) {
        return requirements.stream()
            .anyMatch(x -> x.isPartOf(iface));
    }

    public boolean containsEntire(OperationInterface iface) {
        return requirements.stream()
            .anyMatch(x -> iface.isPartOf(x));
    }

    @Override
    public Iterator<OperationInterface> iterator() {
        return Collections.unmodifiableCollection(requirements)
            .iterator();
    }

    public Map<OperationInterface, List<Operation>> simplified() {
        List<Map<OperationInterface, List<Operation>>> simplifiedInterfaces = new LinkedList<>();
        for (OperationInterface root : groupedRequirements.keySet()) {
            List<Operation> simplifiedRoot = new ArrayList<>(root.simplified()
                .values()
                .stream()
                .flatMap(x -> x.stream())
                .collect(Collectors.toList()));
            for (OperationInterface member : groupedRequirements.get(root)) {
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
        return Objects.hash(requirements);
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
        Requirements other = (Requirements) obj;
        return Objects.equals(requirements, other.requirements);
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
