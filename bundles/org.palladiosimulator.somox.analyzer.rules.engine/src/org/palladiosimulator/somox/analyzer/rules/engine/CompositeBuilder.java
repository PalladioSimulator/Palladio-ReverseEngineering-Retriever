package org.palladiosimulator.somox.analyzer.rules.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.dom.CompilationUnit;

public class CompositeBuilder {

    private String name;
    private Set<CompilationUnit> parts = new HashSet<>();

    public CompositeBuilder(String name) {
        this.name = name;
    }

    public void addPart(CompilationUnit unit) {
        parts.add(unit);
    }

    public Composite construct(Map<CompilationUnit, Set<String>> totalRequirements,
            Map<CompilationUnit, Set<String>> totalProvisions, Set<String> compositeRequirements,
            Set<String> compositeProvisions) {

        // Add all explicit parts.
        Set<CompilationUnit> allParts = new HashSet<>(parts);
        Set<String> internalInterfaces = new HashSet<>();

        int previousPartCount = 0;
        int previousInternalInterfaceCount = 0;
        do {
            previousPartCount = allParts.size();
            previousInternalInterfaceCount = internalInterfaces.size();

            propagateRelations(totalProvisions, compositeProvisions, totalRequirements, allParts, internalInterfaces);
            propagateRelations(totalRequirements, compositeRequirements, totalProvisions, allParts, internalInterfaces);
        } while (allParts.size() > previousPartCount && internalInterfaces.size() > previousInternalInterfaceCount);

        Set<String> requiredInterfaces = new HashSet<>();
        Set<String> providedInterfaces = new HashSet<>();

        for (CompilationUnit part : allParts) {
            requiredInterfaces.addAll(totalRequirements.getOrDefault(part, Set.of()));
            // TODO: This could lead to multiple components providing the same interface.
            providedInterfaces.addAll(totalProvisions.getOrDefault(part, Set.of()));
        }
        requiredInterfaces.removeIf(x -> !compositeRequirements.contains(x));
        providedInterfaces.removeIf(x -> !compositeProvisions.contains(x));

        return new Composite(name, allParts, requiredInterfaces, providedInterfaces, internalInterfaces);
    }

    // Writes to allParts and internalInterfaces.
    // The naming scheme is fixed to requirement propagation for readability, but this function also
    // works for provision propagation. Replace all mentions of "requirement" with "provision" and
    // vice versa.
    private static void propagateRelations(final Map<CompilationUnit, Set<String>> allRequirements,
            final Set<String> compositeRequirements, final Map<CompilationUnit, Set<String>> allProvisions,
            Set<CompilationUnit> allParts, Set<String> internalInterfaces) {

        Map<String, Set<CompilationUnit>> provisionsInverted = invertMap(allRequirements);
        // Do not include interfaces provided by composites.
        // This ensures that composites providing for this composite do not become part of it.
        compositeRequirements.forEach(provisionsInverted::remove);

        // Iterate through all units with provisions. If a unit provides a part for this composite,
        // it also becomes part of it.
        for (CompilationUnit providingUnit : allProvisions.keySet()) {

            List<RelationChain> traversedInterfaces = traceRelationToAPart(allRequirements, allProvisions, allParts,
                    provisionsInverted, providingUnit);

            // A unit is part of this composite if a part the composite requires it.
            if (!traversedInterfaces.isEmpty()) {
                allParts.add(providingUnit);
                traversedInterfaces.forEach(x -> x.addTo(internalInterfaces));
            }
        }
    }

    private static <K, V> Map<V, Set<K>> invertMap(Map<K, Set<V>> map) {
        Map<V, Set<K>> inverted = new HashMap<>();

        for (Entry<K, Set<V>> entry : map.entrySet()) {
            for (V value : entry.getValue()) {
                if (!inverted.containsKey(value)) {
                    inverted.put(value, new HashSet<>());
                }
                inverted.get(value)
                    .add(entry.getKey());
            }
        }

        return inverted;
    }

    private static List<RelationChain> traceRelationToAPart(final Map<CompilationUnit, Set<String>> allRequirements,
            final Map<CompilationUnit, Set<String>> allProvisions, final Set<CompilationUnit> allParts,
            final Map<String, Set<CompilationUnit>> provisionsInverted, final CompilationUnit providingUnit) {

        Stack<RelationChain> unitProvisions = new Stack<>();
        allProvisions.get(providingUnit)
            .stream()
            .map(x -> new RelationChain(x))
            .forEach(unitProvisions::add);

        List<RelationChain> traversedInterfaces = new ArrayList<>();
        while (!unitProvisions.isEmpty()) {
            RelationChain provision = unitProvisions.pop();
            Set<CompilationUnit> providedUnits = provisionsInverted.get(provision.last());

            // Skip this provision if no unit requires it.
            if (providedUnits == null) {
                continue;
            }

            for (CompilationUnit providedUnit : providedUnits) {
                if (allParts.contains(providedUnit)) {
                    traversedInterfaces.add(provision);
                    continue;
                }
                Set<String> transitiveProvisions = new HashSet<>(allRequirements.get(providedUnit));

                // Ignore units requiring themselves.
                transitiveProvisions.remove(provision.last());

                transitiveProvisions.stream()
                    .map(x -> provision.extend(x))
                    .filter(x -> x.isPresent())
                    .map(x -> x.get())
                    .forEach(unitProvisions::push);
            }
        }

        return traversedInterfaces;
    }

    private static class RelationChain {
        // Requirement/Provision path where a given element requires/provides all those following
        // it.
        private final List<String> path;
        private final Set<String> memberSet;

        public RelationChain(String... path) {
            this.path = List.of(path);
            this.memberSet = Set.of(path);
        }

        public String last() {
            return path.get(path.size() - 1);
        }

        public void addTo(Collection<String> collection) {
            collection.addAll(path);
        }

        public Optional<RelationChain> extend(String next) {
            // Avoid relation cycles.
            if (memberSet.contains(next)) {
                return Optional.empty();
            }

            List<String> merged = new ArrayList<>(path);
            merged.add(next);
            return Optional.of(new RelationChain(merged.toArray(new String[0])));
        }

        @Override
        public String toString() {
            return path.toString();
        }
    }
}
