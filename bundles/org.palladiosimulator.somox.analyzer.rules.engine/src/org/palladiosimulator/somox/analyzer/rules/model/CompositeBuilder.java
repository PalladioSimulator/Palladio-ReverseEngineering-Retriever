package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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

    public Composite construct(Collection<Component> components, Set<EntireInterface> compositeRequirements,
            Set<OperationInterface> compositeProvisions) {
        Logger.getLogger(getClass())
            .warn("Constructing composite component " + name);

        // Create and add all explicit parts.
        Set<Component> parts = explicitParts.stream()
            .map(ComponentBuilder::create)
            .collect(Collectors.toSet());

        Set<OperationInterface> internalInterfaces = new HashSet<>();

        int previousPartCount = 0;
        int previousInternalInterfaceCount = 0;
        do {
            previousPartCount = parts.size();
            previousInternalInterfaceCount = internalInterfaces.size();

            propagateProvisions(components, compositeProvisions, parts, internalInterfaces);
            propagateRequirements(components, compositeRequirements, parts, internalInterfaces);
        } while (parts.size() > previousPartCount && internalInterfaces.size() > previousInternalInterfaceCount);

        Set<EntireInterface> requirements = new HashSet<>();
        Set<OperationInterface> provisions = new HashSet<>();

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

        return new Composite(name, parts, requirementsBuilder.create(), provisionsBuilder.create(), internalInterfaces);
    }

    // Writes to allParts and internalInterfaces.
    private static void propagateRequirements(final Collection<Component> allComponents,
            final Set<EntireInterface> compositeRequirements, Set<Component> allParts,
            Set<OperationInterface> internalInterfaces) {

        // Iterate through all units with provisions. If a unit provides a part for this composite,
        // it also becomes part of it.
        for (Component requiringComponent : allComponents) {
            List<InterfaceChain> traversedInterfaces = traceRequirementToAPart(allComponents, compositeRequirements,
                    allParts, requiringComponent);

            // A unit is part of this composite if a part of the composite requires it.
            if (!traversedInterfaces.isEmpty()) {
                allParts.add(requiringComponent);
                traversedInterfaces.forEach(x -> x.addTo(internalInterfaces));
            }
        }
    }

    // Writes to allParts and internalOperations.
    private static void propagateProvisions(final Collection<Component> allComponents,
            final Set<OperationInterface> compositeProvisions, Set<Component> allParts,
            Set<OperationInterface> internalOperations) {

        // Iterate through all units with provisions. If a unit provides a part for this composite,
        // it also becomes part of it.
        for (Component providingComponent : allComponents) {

            List<InterfaceChain> traversedOperations = traceProvisionToAPart(allComponents, compositeProvisions,
                    allParts, providingComponent);

            // A unit is part of this composite if a part the composite requires it.
            if (!traversedOperations.isEmpty()) {
                allParts.add(providingComponent);
                traversedOperations.forEach(x -> x.addTo(internalOperations));
            }
        }
    }

    private static List<InterfaceChain> traceRequirementToAPart(final Collection<Component> allComponents,
            final Set<EntireInterface> compositeRequirements, final Set<Component> allParts,
            final Component requiringComponent) {

        Stack<InterfaceChain> componentRequirements = new Stack<>();
        requiringComponent.requirements()
            .get()
            .stream()
            // Do not include interfaces required by composites.
            // This ensures that composites providing for this composite do not become part of it.
            .filter(x -> !compositeRequirements.contains(x))
            .map(x -> new InterfaceChain(x))
            .forEach(componentRequirements::add);

        List<InterfaceChain> traversedOperations = new ArrayList<>();
        while (!componentRequirements.isEmpty()) {
            InterfaceChain requirementChain = componentRequirements.pop();
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

    private static List<InterfaceChain> traceProvisionToAPart(final Collection<Component> allComponents,
            Set<OperationInterface> compositeProvisions, final Set<Component> allParts,
            final Component providingComponent) {

        Stack<InterfaceChain> componentProvisions = new Stack<>();
        providingComponent.provisions()
            .get()
            .stream()
            // Do not include interfaces provided by composites.
            // This ensures that composites requiring this composite
            // do not become part of it.
            .filter(x -> !compositeProvisions.contains(x))
            .map(x -> new InterfaceChain(x))
            .forEach(componentProvisions::add);

        List<InterfaceChain> traversedOperations = new ArrayList<>();
        while (!componentProvisions.isEmpty()) {
            InterfaceChain provisionChain = componentProvisions.pop();

            Set<Component> requiringComponents = allComponents.stream()
                .filter(x -> x.requirements()
                    .contains(provisionChain.last()))
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

    private static class InterfaceChain {
        // Provision/Requirement path where a given element provides/requires all those following
        // it.
        private final List<OperationInterface> path;
        private final Set<OperationInterface> memberSet;

        public InterfaceChain(OperationInterface... path) {
            this.path = List.of(path);
            this.memberSet = Set.of(path);
        }

        public OperationInterface last() {
            return path.get(path.size() - 1);
        }

        public void addTo(Collection<OperationInterface> collection) {
            collection.addAll(path);
        }

        public Optional<InterfaceChain> extend(OperationInterface next) {
            // Avoid relation cycles.
            if (memberSet.contains(next)) {
                return Optional.empty();
            }

            List<OperationInterface> merged = new ArrayList<>(path);
            merged.add(next);
            return Optional.of(new InterfaceChain(merged.toArray(new OperationInterface[0])));
        }

        @Override
        public String toString() {
            return path.toString();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(explicitParts, name);
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
        CompositeBuilder other = (CompositeBuilder) obj;
        return Objects.equals(explicitParts, other.explicitParts) && Objects.equals(name, other.name);
    }
}
