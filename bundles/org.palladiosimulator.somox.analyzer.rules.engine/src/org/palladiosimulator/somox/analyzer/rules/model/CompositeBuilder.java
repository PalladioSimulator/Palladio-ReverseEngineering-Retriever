package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

public class CompositeBuilder {

    private String name;
    private Set<ComponentBuilder> explicitParts = new HashSet<>();

    public CompositeBuilder(String name) {
        this.name = name;
    }

    public void addPart(ComponentBuilder componentBuilder) {
        explicitParts.add(componentBuilder);
    }

    public Composite construct(Collection<Component> components, Set<String> compositeRequirements,
            Set<Provision> compositeProvisions) {
        Logger.getLogger(getClass())
            .warn("Constructing composite component " + name);

        // Create and add all explicit parts.
        Set<Component> parts = explicitParts.stream()
            .map(ComponentBuilder::create)
            .collect(Collectors.toSet());

        Set<String> internalRequirements = new HashSet<>();
        Set<Provision> internalProvisions = new HashSet<>();

        int previousPartCount = 0;
        int previousInternalInterfaceCount = 0;
        do {
            previousPartCount = parts.size();
            previousInternalInterfaceCount = internalRequirements.size();

            propagateProvisions(components, compositeProvisions, parts, internalProvisions);
            propagateRequirements(components, compositeRequirements, parts, internalRequirements);
        } while (parts.size() > previousPartCount && internalRequirements.size() > previousInternalInterfaceCount);

        Set<String> requirements = new HashSet<>();
        Set<Provision> provisions = new HashSet<>();

        for (Component part : parts) {
            requirements.addAll(part.requirements()
                .get());
            provisions.addAll(part.provisions()
                .get());
        }
        requirements.removeIf(x -> !compositeRequirements.contains(x));
        RequirementsBuilder requirementsBuilder = new RequirementsBuilder();
        requirementsBuilder.add(requirements);

        provisions.removeIf(x -> !compositeProvisions.contains(x));
        ProvisionsBuilder provisionsBuilder = new ProvisionsBuilder();
        provisionsBuilder.add(provisions);

        return new Composite(name, parts, requirementsBuilder.create(), provisionsBuilder.create(),
                internalRequirements);
    }

    // Writes to allParts and internalInterfaces.
    private static void propagateRequirements(final Collection<Component> allComponents,
            final Set<String> compositeRequirements, Set<Component> allParts, Set<String> internalInterfaces) {

        // Iterate through all units with provisions. If a unit provides a part for this composite,
        // it also becomes part of it.
        for (Component requiringComponent : allComponents) {
            List<RequirementChain> traversedInterfaces = traceRequirementToAPart(allComponents, compositeRequirements,
                    allParts, requiringComponent);

            // A unit is part of this composite if a part the composite requires it.
            if (!traversedInterfaces.isEmpty()) {
                allParts.add(requiringComponent);
                traversedInterfaces.forEach(x -> x.addTo(internalInterfaces));
            }
        }
    }

    // Writes to allParts and internalOperations.
    private static void propagateProvisions(final Collection<Component> allComponents,
            final Set<Provision> compositeProvisions, Set<Component> allParts, Set<Provision> internalOperations) {

        // Iterate through all units with provisions. If a unit provides a part for this composite,
        // it also becomes part of it.
        for (Component providingComponent : allComponents) {

            List<ProvisionChain> traversedOperations = traceProvisionToAPart(allComponents, compositeProvisions,
                    allParts, providingComponent);

            // A unit is part of this composite if a part the composite requires it.
            if (!traversedOperations.isEmpty()) {
                allParts.add(providingComponent);
                traversedOperations.forEach(x -> x.addTo(internalOperations));
            }
        }
    }

    private static List<RequirementChain> traceRequirementToAPart(final Collection<Component> allComponents,
            final Set<String> compositeRequirements, final Set<Component> allParts,
            final Component requiringComponent) {

        Stack<RequirementChain> componentRequirements = new Stack<>();
        requiringComponent.requirements()
            .get()
            .stream()
            // Do not include interfaces required by composites.
            // This ensures that composites providing for this composite do not become part of it.
            .filter(x -> !compositeRequirements.contains(x))
            .map(x -> new RequirementChain(x))
            .forEach(componentRequirements::add);

        List<RequirementChain> traversedOperations = new ArrayList<>();
        while (!componentRequirements.isEmpty()) {
            RequirementChain requirementChain = componentRequirements.pop();
            Set<Component> providingComponents = allComponents.stream()
                .filter(x -> x.provisions()
                    .contains(requirementChain.last()))
                .filter(x -> !requiringComponent.equals(x))
                .collect(Collectors.toSet());

            // Skip this requirement if no unit provides it.
            if (providingComponents.isEmpty()) {
                continue;
            }

            for (Component providingComponent : providingComponents) {
                if (allParts.contains(providingComponent)) {
                    traversedOperations.add(requirementChain);
                    continue;
                }

                providingComponent.requirements()
                    .get()
                    .stream()
                    .filter(x -> !compositeRequirements.contains(x))
                    .map(x -> requirementChain.extend(x))
                    .filter(x -> x.isPresent())
                    .map(x -> x.get())
                    .forEach(componentRequirements::push);
            }
        }

        return traversedOperations;
    }

    private static List<ProvisionChain> traceProvisionToAPart(final Collection<Component> allComponents,
            Set<Provision> compositeProvisions, final Set<Component> allParts, final Component providingComponent) {

        Stack<ProvisionChain> componentProvisions = new Stack<>();
        providingComponent.provisions()
            .get()
            .stream()
            // Do not include interfaces provided by composites.
            // This ensures that composites requiring this composite
            // do not become part of it.
            .filter(x -> !compositeProvisions.contains(x))
            .map(x -> new ProvisionChain(x))
            .forEach(componentProvisions::add);

        List<ProvisionChain> traversedOperations = new ArrayList<>();
        while (!componentProvisions.isEmpty()) {
            ProvisionChain provisionChain = componentProvisions.pop();

            Set<Component> requiringComponents = allComponents.stream()
                .filter(x -> x.requirements()
                    .contains(provisionChain.last()
                        .getInterface()))
                .filter(x -> providingComponent.equals(x))
                .collect(Collectors.toSet());

            // Skip this provision if no unit requires it.
            if (requiringComponents.isEmpty()) {
                continue;
            }

            for (Component requiringComponent : requiringComponents) {
                if (allParts.contains(requiringComponent)) {
                    traversedOperations.add(provisionChain);
                    continue;
                }

                requiringComponent.provisions()
                    .get()
                    .stream()
                    .filter(x -> !compositeProvisions.contains(x))
                    .map(x -> provisionChain.extend(x))
                    .filter(x -> x.isPresent())
                    .map(x -> x.get())
                    .forEach(componentProvisions::push);
            }
        }

        return traversedOperations;
    }

    private static class ProvisionChain {
        // Provision path where a given element provides all those following it.
        private final List<Provision> path;
        private final Set<Provision> memberSet;

        public ProvisionChain(Provision... path) {
            this.path = List.of(path);
            this.memberSet = Set.of(path);
        }

        public Provision last() {
            return path.get(path.size() - 1);
        }

        public void addTo(Collection<Provision> collection) {
            collection.addAll(path);
        }

        public Optional<ProvisionChain> extend(Provision next) {
            // Avoid relation cycles.
            if (memberSet.contains(next)) {
                return Optional.empty();
            }

            List<Provision> merged = new ArrayList<>(path);
            merged.add(next);
            return Optional.of(new ProvisionChain(merged.toArray(new Provision[0])));
        }

        @Override
        public String toString() {
            return path.toString();
        }
    }

    private static class RequirementChain {
        // Requirement/Provision path where a given element requires/provides all those following
        // it.
        private final List<String> path;
        private final Set<String> memberSet;

        public RequirementChain(String... path) {
            this.path = List.of(path);
            this.memberSet = Set.of(path);
        }

        public String last() {
            return path.get(path.size() - 1);
        }

        public void addTo(Collection<String> collection) {
            collection.addAll(path);
        }

        public Optional<RequirementChain> extend(String next) {
            // Avoid relation cycles.
            if (memberSet.contains(next)) {
                return Optional.empty();
            }

            List<String> merged = new ArrayList<>(path);
            merged.add(next);
            return Optional.of(new RequirementChain(merged.toArray(new String[0])));
        }

        @Override
        public String toString() {
            return path.toString();
        }
    }
}
