package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
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

    public Composite construct(Collection<Component> components, Requirements compositeRequirements,
            Provisions compositeProvisions) {
        Logger.getLogger(getClass())
            .warn("Constructing composite component " + name);

        // Create and add all explicit parts.
        Set<Component> parts = explicitParts.stream()
            .map(ComponentBuilder::create)
            .collect(Collectors.toSet());

        Set<Component> remainingComponents = new HashSet<>(components);
        remainingComponents.removeAll(parts);
        Set<OperationInterface> internalInterfaces = new HashSet<>();

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

    // Writes to remainingComopnents, parts, and internalInterfaces.
    private static void propagateProvisions(Set<Component> remainingComponents,
            final Requirements compositeRequirements, final Provisions compositeProvisions, Set<Component> parts,
            Set<OperationInterface> internalInterfaces) {

        List<Component> newParts = new LinkedList<>();
        for (Component providingPart : parts) {
            List<OperationInterface> traversedInterfaces = findRequiringComponents(remainingComponents,
                    compositeRequirements, compositeProvisions, newParts, providingPart);
            internalInterfaces.addAll(traversedInterfaces);
        }

        parts.addAll(newParts);
    }

    // Writes to remainingComopnents, parts, and internalInterfaces.
    private static void propagateRequirements(Set<Component> remainingComponents,
            final Requirements compositeRequirements, final Provisions compositeProvisions, Set<Component> parts,
            Set<OperationInterface> internalInterfaces) {

        List<Component> newParts = new LinkedList<>();
        for (Component requiringPart : parts) {
            List<OperationInterface> traversedInterfaces = findProvidingComponents(remainingComponents,
                    compositeRequirements, compositeProvisions, newParts, requiringPart);
            internalInterfaces.addAll(traversedInterfaces);
        }

        parts.addAll(newParts);
    }

    // May remove components from remainingComponents.
    private static List<OperationInterface> findRequiringComponents(Set<Component> remainingComponents,
            final Requirements compositeRequirements, final Provisions compositeProvisions, List<Component> newParts,
            final Component providingComponent) {

        Stack<OperationInterface> provisions = new Stack<>();
        providingComponent.provisions()
            .get()
            .stream()
            // Do not include interfaces required or provided by composites.
            // This ensures that those composites do not become part of this composite.
            .filter(x -> !compositeRequirements.contains(x))
            .filter(x -> !compositeProvisions.contains(x))
            .forEach(provisions::add);

        List<OperationInterface> traversedOperations = new ArrayList<>();
        while (!provisions.isEmpty()) {
            OperationInterface provision = provisions.pop();
            Set<Component> requiringComponents = remainingComponents.stream()
                .filter(x -> x.requirements()
                    .contains(provision))
                .filter(x -> !providingComponent.equals(x))
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
    private static List<OperationInterface> findProvidingComponents(Set<Component> remainingComponents,
            final Requirements compositeRequirements, final Provisions compositeProvisions, List<Component> newParts,
            final Component requiringComponent) {

        Stack<OperationInterface> requirements = new Stack<>();
        requiringComponent.requirements()
            .get()
            .stream()
            // Do not include interfaces required or provided by composites.
            // This ensures that those composites do not become part of this composite.
            .filter(x -> !compositeRequirements.contains(x))
            .filter(x -> !compositeProvisions.contains(x))
            .forEach(requirements::add);

        List<OperationInterface> traversedOperations = new ArrayList<>();
        while (!requirements.isEmpty()) {
            OperationInterface requirement = requirements.pop();
            Set<Component> providingComponents = remainingComponents.stream()
                .filter(x -> x.provisions()
                    .contains(requirement))
                .filter(x -> !requiringComponent.equals(x))
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
