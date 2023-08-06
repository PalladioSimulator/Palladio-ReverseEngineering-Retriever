package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.palladiosimulator.somox.analyzer.rules.engine.MapMerger;

public class Requirements implements Iterable<EntireInterface> {
    private final Set<EntireInterface> requirements;

    public Requirements(Collection<EntireInterface> requiredInterfaces) {
        this.requirements = new HashSet<>(requiredInterfaces);
    }

    public Set<EntireInterface> get() {
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
    public Iterator<EntireInterface> iterator() {
        return Collections.unmodifiableCollection(requirements)
            .iterator();
    }

    public Map<String, List<Operation>> simplified() {
        List<Map<String, List<Operation>>> simplifiedInterfaces = requirements.stream()
            .map(OperationInterface::simplified)
            .collect(Collectors.toList());

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
        Map<String, List<Operation>> simplified = simplified();

        for (String iface : simplified.keySet()) {
            builder.append(iface);
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
