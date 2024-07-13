package org.palladiosimulator.retriever.extraction.commonalities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.palladiosimulator.retriever.extraction.engine.MapMerger;

public class CompositeBuilder {

    private final String name;
    private final Set<ComponentBuilder> explicitParts = new HashSet<>();

    public CompositeBuilder(final String name) {
        this.name = name;
    }

    public void addPart(final ComponentBuilder componentBuilder) {
        this.explicitParts.add(componentBuilder);
    }

    public boolean hasPart(final CompUnitOrName identifier) {
        return this.explicitParts.stream()
            .anyMatch(part -> part.identifier()
                .equals(identifier));
    }

    public Collection<ComponentBuilder> getParts() {
        return Set.copyOf(this.explicitParts);
    }

    public Composite construct(final Collection<Component> allComponents, final Requirements compositeRequirements,
            final Provisions compositeProvisions, final Collection<OperationInterface> visibleProvisions) {
        Logger.getLogger(this.getClass())
            .warn("Constructing composite component " + this.name);

        final List<OperationInterface> allDependencies = new LinkedList<>();
        for (final OperationInterface requirement : compositeRequirements) {
            allDependencies.add(requirement);
        }
        for (final OperationInterface provision : compositeProvisions) {
            allDependencies.add(provision);
        }

        // Create and add all explicit parts.
        final Set<Component> parts = this.explicitParts.stream()
            .map(x -> x.create(allDependencies, visibleProvisions))
            .collect(Collectors.toSet());

        final Set<Component> remainingComponents = new HashSet<>(allComponents);
        remainingComponents.removeAll(parts);
        final Set<OperationInterface> internalInterfaces = new HashSet<>();

        int previousPartCount = 0;
        int previousInternalInterfaceCount = 0;
        do {
            previousPartCount = parts.size();
            previousInternalInterfaceCount = internalInterfaces.size();

            propagateRequirements(remainingComponents, compositeRequirements, compositeProvisions, parts,
                    internalInterfaces);
            propagateProvisions(remainingComponents, compositeRequirements, compositeProvisions, parts,
                    internalInterfaces);
        } while (parts.size() > previousPartCount && internalInterfaces.size() > previousInternalInterfaceCount);

        final List<OperationInterface> requirements = new ArrayList<>();
        final List<Map<OperationInterface, List<OperationInterface>>> provisions = new ArrayList<>();

        List<String> partNames = new LinkedList<>();
        for (final Component part : parts) {
            requirements.addAll(part.requirements()
                .get());
            provisions.add(part.provisions()
                .getGrouped());
            partNames.add(part.name());
        }

        // Derive Composite name.
        Map<String, Integer> prefixes = new HashMap<>();
        for (String partName : partNames) {
            String prefix = "";
            for (String nameSegment : partName.split("\\.")) {
                if (!prefix.isEmpty()) {
                    prefix += ".";
                }
                prefix += nameSegment;
                prefixes.put(prefix, 1 + prefixes.getOrDefault(prefix, 0));
            }
        }

        int maxSupport = prefixes.entrySet()
            .stream()
            .max((a, b) -> a.getValue()
                .compareTo(b.getValue()))
            .map(x -> x.getValue())
            .orElse(0);

        String chosenPrefix = prefixes.entrySet()
            .stream()
            .filter(x -> x.getValue()
                .equals(maxSupport))
            .map(x -> x.getKey())
            .max(Comparator.comparing(x -> x.length()))
            .orElse(name);

        Logger.getLogger(this.getClass())
            .warn("Chose name " + chosenPrefix + " supported by " + maxSupport + "/" + parts.size() + " parts.");

        final Set<OperationInterface> externalRequirements = requirements.stream()
            .filter(x -> compositeRequirements.containsEntire(x))
            .collect(Collectors.toSet());

        final Set<OperationInterface> externalProvisions = MapMerger.merge(provisions)
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue()
                .stream()
                .anyMatch(operation -> compositeProvisions.containsEntire(operation)))
            .map(entry -> entry.getKey())
            .collect(Collectors.toSet());

        return new Composite(chosenPrefix, parts, externalRequirements, externalProvisions, internalInterfaces);
    }

    // Writes to remainingComopnents, parts, and internalInterfaces.
    private static void propagateProvisions(final Set<Component> remainingComponents,
            final Requirements compositeRequirements, final Provisions compositeProvisions, final Set<Component> parts,
            final Set<OperationInterface> internalInterfaces) {

        final List<Component> newParts = new LinkedList<>();
        for (final Component providingPart : parts) {
            final List<OperationInterface> traversedInterfaces = findRequiringComponents(remainingComponents,
                    compositeRequirements, compositeProvisions, newParts, providingPart);

            final Queue<OperationInterface> sortedInterfaces = new PriorityQueue<>(traversedInterfaces);
            while (!sortedInterfaces.isEmpty()) {
                final OperationInterface iface = sortedInterfaces.poll();
                boolean isRoot = true;
                for (final OperationInterface rootInterface : internalInterfaces) {
                    if (iface.isPartOf(rootInterface)) {
                        isRoot = false;
                        break;
                    }
                    if (rootInterface.isPartOf(iface)) {
                        internalInterfaces.remove(rootInterface);
                        break;
                    }
                }
                if (isRoot) {
                    internalInterfaces.add(iface);
                }
            }
        }

        parts.addAll(newParts);
    }

    // Writes to remainingComopnents, parts, and internalInterfaces.
    private static void propagateRequirements(final Set<Component> remainingComponents,
            final Requirements compositeRequirements, final Provisions compositeProvisions, final Set<Component> parts,
            final Set<OperationInterface> internalInterfaces) {

        final List<Component> newParts = new LinkedList<>();
        for (final Component requiringPart : parts) {
            final List<OperationInterface> traversedInterfaces = findProvidingComponents(remainingComponents,
                    compositeRequirements, compositeProvisions, newParts, requiringPart);

            final Queue<OperationInterface> sortedInterfaces = new PriorityQueue<>(traversedInterfaces);
            while (!sortedInterfaces.isEmpty()) {
                final OperationInterface iface = sortedInterfaces.poll();
                boolean isRoot = true;
                for (final OperationInterface rootInterface : internalInterfaces) {
                    if (iface.isPartOf(rootInterface)) {
                        isRoot = false;
                        break;
                    }
                    if (rootInterface.isPartOf(iface)) {
                        internalInterfaces.remove(rootInterface);
                        break;
                    }
                }
                if (isRoot) {
                    internalInterfaces.add(iface);
                }
            }
        }

        parts.addAll(newParts);
    }

    // May remove components from remainingComponents.
    private static List<OperationInterface> findRequiringComponents(final Set<Component> remainingComponents,
            final Requirements compositeRequirements, final Provisions compositeProvisions,
            final List<Component> newParts, final Component providingComponent) {

        final Stack<OperationInterface> provisions = new Stack<>();
        providingComponent.provisions()
            .get()
            .stream()
            // Do not include interfaces required or provided by composites.
            // This ensures that those composites do not become part of this composite.
            .filter(x -> !compositeRequirements.containsEntire(x))
            .filter(x -> !compositeProvisions.containsEntire(x))
            .forEach(provisions::add);

        final Optional<String> separatingIdentifier = providingComponent.separatingIdentifier();

        final List<OperationInterface> traversedOperations = new ArrayList<>();
        while (!provisions.isEmpty()) {
            final OperationInterface provision = provisions.pop();
            final Set<Component> requiringComponents = remainingComponents.stream()
                .filter(x -> x.requirements()
                    .containsPartOf(provision))
                .filter(x -> !providingComponent.equals(x))
                .filter(x -> x.separatingIdentifier()
                    .isEmpty() || separatingIdentifier.isEmpty()
                        || x.separatingIdentifier()
                            .equals(separatingIdentifier))
                .collect(Collectors.toSet());

            // Skip this provision if no unit requires it.
            if (!requiringComponents.isEmpty()) {
                traversedOperations.add(provision);
                remainingComponents.removeAll(requiringComponents);
                newParts.addAll(requiringComponents);
            }
        }

        return traversedOperations;
    }

    // May remove components from remainingComponents.
    private static List<OperationInterface> findProvidingComponents(final Set<Component> remainingComponents,
            final Requirements compositeRequirements, final Provisions compositeProvisions,
            final List<Component> newParts, final Component requiringComponent) {

        final Stack<OperationInterface> requirements = new Stack<>();
        requiringComponent.requirements()
            .get()
            .stream()
            // Do not include interfaces required or provided by composites.
            // This ensures that those composites do not become part of this composite.
            .filter(x -> !compositeRequirements.containsEntire(x))
            .filter(x -> !compositeProvisions.containsEntire(x))
            .forEach(requirements::add);

        final Optional<String> separatingIdentifier = requiringComponent.separatingIdentifier();

        final List<OperationInterface> traversedOperations = new ArrayList<>();
        while (!requirements.isEmpty()) {
            final OperationInterface requirement = requirements.pop();
            final Set<Component> providingComponents = remainingComponents.stream()
                .filter(x -> x.provisions()
                    .containsPartOf(requirement))
                .filter(x -> !requiringComponent.equals(x))
                .filter(x -> x.separatingIdentifier()
                    .isEmpty() || separatingIdentifier.isEmpty()
                        || x.separatingIdentifier()
                            .equals(separatingIdentifier))
                .collect(Collectors.toSet());

            // Skip this requirement if no unit provides it.
            if (!providingComponents.isEmpty()) {
                traversedOperations.add(requirement);
                remainingComponents.removeAll(providingComponents);
                newParts.addAll(providingComponents);
            }
        }

        return traversedOperations;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.explicitParts, this.name);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (this.getClass() != obj.getClass())) {
            return false;
        }
        final CompositeBuilder other = (CompositeBuilder) obj;
        return Objects.equals(this.explicitParts, other.explicitParts) && Objects.equals(this.name, other.name);
    }
}
