package org.palladiosimulator.somox.analyzer.rules.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.palladiosimulator.somox.analyzer.rules.model.Component;
import org.palladiosimulator.somox.analyzer.rules.model.ComponentBuilder;
import org.palladiosimulator.somox.analyzer.rules.model.Operation;
import org.palladiosimulator.somox.analyzer.rules.model.ProvisionsBuilder;
import org.palladiosimulator.somox.analyzer.rules.model.RequirementsBuilder;

public class CompositeBuilder {

    private String name;
    private Set<ComponentBuilder> explicitParts = new HashSet<>();

    public CompositeBuilder(String name) {
        this.name = name;
    }

    public void addPart(ComponentBuilder componentBuilder) {
        explicitParts.add(componentBuilder);
    }

    public Composite construct(Collection<ComponentBuilder> components, Set<String> compositeRequirements,
            Set<Operation> compositeProvisions) {
        Logger.getLogger(getClass())
            .warn("Constructing composite component " + name);

        Collection<Component> builtComponents = components.stream()
            .map(ComponentBuilder::create)
            .collect(Collectors.toSet());

        // Create and add all explicit parts.
        Set<Component> parts = explicitParts.stream()
            .map(ComponentBuilder::create)
            .collect(Collectors.toSet());

        Set<String> internalInterfaces = new HashSet<>();
        Set<Operation> internalOperations = new HashSet<>();

        int previousPartCount = 0;
        int previousInternalInterfaceCount = 0;
        do {
            previousPartCount = parts.size();
            previousInternalInterfaceCount = internalInterfaces.size();

            propagateProvisions(builtComponents, compositeProvisions, parts, internalOperations);
            propagateRequirements(builtComponents, compositeRequirements, parts, internalInterfaces);
        } while (parts.size() > previousPartCount && internalInterfaces.size() > previousInternalInterfaceCount);

        Set<String> requiredInterfaces = new HashSet<>();
        Set<Operation> providedOperations = new HashSet<>();

        for (Component part : parts) {
            requiredInterfaces.addAll(part.requirements()
                .get());
            providedOperations.addAll(part.provisions()
                .get());
        }
        requiredInterfaces.removeIf(x -> !compositeRequirements.contains(x));
        RequirementsBuilder requirements = new RequirementsBuilder();
        requirements.add(requiredInterfaces);

        providedOperations.removeIf(x -> !compositeProvisions.contains(x));
        ProvisionsBuilder provisions = new ProvisionsBuilder();
        provisions.add(providedOperations);

        return new Composite(name, parts, requirements.create(), provisions.create(), internalInterfaces);
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
            final Set<Operation> compositeProvisions, Set<Component> allParts, Set<Operation> internalOperations) {

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

                // Ignore units requiring themselves.
                // TODO: Can this still happen?
                // transitiveProvisions.remove(provision.last());

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
            Set<Operation> compositeProvisions, final Set<Component> allParts, final Component providingComponent) {

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
                    .contains(provisionChain.last()))
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

                // Ignore units requiring themselves.
                // TODO: Can this still happen?
                // transitiveProvisions.remove(provision.last());

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
        private final List<Operation> path;
        private final Set<Operation> memberSet;

        public ProvisionChain(Operation... path) {
            this.path = List.of(path);
            this.memberSet = Set.of(path);
        }

        public Operation last() {
            return path.get(path.size() - 1);
        }

        public void addTo(Collection<Operation> collection) {
            collection.addAll(path);
        }

        public Optional<ProvisionChain> extend(Operation next) {
            // Avoid relation cycles.
            if (memberSet.contains(next)) {
                return Optional.empty();
            }

            List<Operation> merged = new ArrayList<>(path);
            merged.add(next);
            return Optional.of(new ProvisionChain(merged.toArray(new Operation[0])));
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
