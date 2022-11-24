package org.palladiosimulator.somox.analyzer.rules.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

        Map<String, Set<CompilationUnit>> provisionsInverted = new HashMap<>();

        for (Entry<CompilationUnit, Set<String>> entry : totalProvisions.entrySet()) {
            for (String provision : entry.getValue()) {
                if (compositeProvisions.contains(provision)) {
                    // Do not include interfaces provided or required by composites.
                    // This ensures that components requiring this composite do not become part of
                    // it.
                    continue;
                }
                if (!provisionsInverted.containsKey(provision)) {
                    provisionsInverted.put(provision, new HashSet<>());
                }
                provisionsInverted.get(provision)
                    .add(entry.getKey());
            }
        }

        // Iterate through all units with requirements. If a unit requires a part of this composite,
        // it also becomes part of it.
        for (CompilationUnit requiringUnit : totalRequirements.keySet()) {

            Stack<Helper> unitRequirements = new Stack<>();
            totalRequirements.get(requiringUnit)
                .stream()
                .map(x -> new Helper(x))
                .forEach(unitRequirements::add);

            List<Helper> traversedInterfaces = new ArrayList<>();

            boolean isPart = false;
            while (!unitRequirements.isEmpty() && !isPart) {
                Helper requirement = unitRequirements.pop();
                Set<CompilationUnit> requiredUnits = provisionsInverted.get(requirement.last());

                if (requiredUnits == null) {
                    // Skip this requirement if no unit provides it.
                    // TODO: why does this happen?
                    continue;
                }

                for (CompilationUnit requiredUnit : requiredUnits) {
                    if (allParts.contains(requiredUnit)) {
                        isPart = true;
                        traversedInterfaces.add(requirement);
                        continue;
                    }
                    Set<String> transitiveRequirements = new HashSet<>(totalProvisions.get(requiredUnit));

                    // Ignore units requiring themselves.
                    transitiveRequirements.remove(requirement.last());

                    transitiveRequirements.stream()
                        .map(x -> requirement.extend(x))
                        .forEach(unitRequirements::push);
                }
            }

            // A unit is part of this composite if it depends on another part of it.
            if (isPart) {
                allParts.add(requiringUnit);
                traversedInterfaces.forEach(x -> x.addTo(internalInterfaces));
            }
        }

        Set<String> requiredInterfaces = new HashSet<>();
        Set<String> providedInterfaces = new HashSet<>();

        for (CompilationUnit part : allParts) {
            requiredInterfaces.addAll(totalRequirements.getOrDefault(part, Set.of()));
            // TODO: This could lead to multiple components providing the same interface.
            providedInterfaces.addAll(totalProvisions.getOrDefault(part, Set.of()));
        }
        requiredInterfaces.removeAll(internalInterfaces);
        providedInterfaces.removeAll(internalInterfaces);

        return new Composite(name, allParts, requiredInterfaces, providedInterfaces, internalInterfaces);
    }

    private class Helper {
        // Requirement path where a given element requires all those following it.
        private final List<String> path;

        public Helper(String... path) {
            this.path = List.of(path);
        }

        public String last() {
            return path.get(path.size() - 1);
        }

        public void addTo(Collection<String> collection) {
            collection.addAll(path);
        }

        public Helper extend(String parent) {
            List<String> merged = new ArrayList<>(path);
            merged.add(parent);
            return new Helper(merged.toArray(new String[0]));
        }

        @Override
        public String toString() {
            return path.toString();
        }
    }
}
