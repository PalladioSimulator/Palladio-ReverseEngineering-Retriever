package org.palladiosimulator.retriever.extraction.commonalities;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.palladiosimulator.retriever.extraction.engine.MapMerger;

public class PCMDetectionResult {
    private final Set<Component> components;
    private final Set<Composite> composites;
    private final Map<OperationInterface, Set<Operation>> operationInterfaces;

    public PCMDetectionResult(final Map<CompUnitOrName, ComponentBuilder> components,
            final Map<String, CompositeBuilder> composites, final ProvisionsBuilder compositeProvisions,
            final RequirementsBuilder compositeRequirements) {

        // Collect globally visible provisions
        final Set<Component> temporaryComponents = PCMDetectionResult.createComponents(components, compositeProvisions,
                compositeRequirements, Set.of());
        final Set<Component> connectedComponents = PCMDetectionResult.collectConnectedComponents(temporaryComponents,
                composites, compositeProvisions, compositeRequirements);
        final Set<Composite> temporaryComposites = PCMDetectionResult.createCompositeComponents(connectedComponents,
                composites, compositeProvisions, compositeRequirements, Set.of());
        final Set<OperationInterface> visibleProvisions = PCMDetectionResult
            .collectVisibleProvisions(connectedComponents, temporaryComposites);

        // TODO: Do not rebuild everything, that is theoretically not necessary since provisions do
        // not change.

        final Map<CompUnitOrName, ComponentBuilder> connectedComponentBuilders = connectedComponents.stream()
            .map(Component::identifier)
            .map(components::get)
            .collect(Collectors.toMap(ComponentBuilder::identifier, x -> x));

        // Construct final result
        this.components = PCMDetectionResult.createComponents(connectedComponentBuilders, compositeProvisions,
                compositeRequirements, visibleProvisions);
        this.composites = PCMDetectionResult.createCompositeComponents(this.components, composites, compositeProvisions,
                compositeRequirements, visibleProvisions);
        this.operationInterfaces = this.createOperationInterfaces();
    }

    private static Set<Component> collectConnectedComponents(final Set<Component> temporaryComponents,
            final Map<String, CompositeBuilder> composites, final ProvisionsBuilder compositeProvisions,
            final RequirementsBuilder compositeRequirements) {
        final CompositeBuilder metaCompositeBuilder = new CompositeBuilder("Meta Composite");
        for (final CompositeBuilder composite : composites.values()) {
            for (final ComponentBuilder part : composite.getParts()) {
                metaCompositeBuilder.addPart(part);
            }
        }
        final Composite metaComposite = metaCompositeBuilder.construct(temporaryComponents,
                new RequirementsBuilder().create(Set.of(), Set.of()), new ProvisionsBuilder().create(Set.of()),
                Set.of());
        final Set<Component> connectedComponents = metaComposite.parts();
        if (connectedComponents.isEmpty()) {
            return temporaryComponents;
        } else {
            return connectedComponents;
        }
    }

    private static Set<Component> createComponents(final Map<CompUnitOrName, ComponentBuilder> components,
            final ProvisionsBuilder compositeProvisions, final RequirementsBuilder compositeRequirements,
            final Set<OperationInterface> visibleProvisions) {
        final List<OperationInterface> allDependencies = new LinkedList<>();
        // TODO: Aren't the dependencies of free components missing here? Is that alright?
        allDependencies.addAll(compositeRequirements.toList());
        allDependencies.addAll(compositeProvisions.toList());

        return components.values()
            .stream()
            .map(x -> x.create(allDependencies, visibleProvisions))
            .collect(Collectors.toSet());
    }

    private static Set<Composite> createCompositeComponents(final Set<Component> freeComponents,
            final Map<String, CompositeBuilder> composites, final ProvisionsBuilder compositeProvisions,
            final RequirementsBuilder compositeRequirements, final Set<OperationInterface> visibleProvisions) {

        // Construct composites.
        final List<Composite> allComposites = composites.values()
            .stream()
            .map(x -> x.construct(freeComponents, compositeRequirements.create(visibleProvisions, visibleProvisions),
                    compositeProvisions.create(visibleProvisions), visibleProvisions))
            .collect(Collectors.toList());

        // Remove redundant composites.
        final Set<Composite> redundantComposites = new HashSet<>();
        final Set<Composite> remainingComposites = new HashSet<>();

        for (final Composite subject : allComposites) {
            final Optional<Composite> other = allComposites.stream()
                .filter(x -> !subject.equals(x))
                .filter(x -> !redundantComposites.contains(x))
                .filter(x -> subject.isSubsetOf(x))
                .findFirst();

            if (other.isPresent()) {
                redundantComposites.add(subject);
                continue;
            } else {
                // TODO: Is there any merging necessary, like adapting the redundant composite's
                // requirements to its peer?
                remainingComposites.add(subject);
            }
        }

        // Remove composite components contained in multiple other composites, according to a
        // conservative heuristic.
        // TODO: A comprehensive solution would require e.g. graph traversal and tie-breaking in
        // cycles.

        final Set<Composite> collectivelyContainedComposites = new HashSet<>();
        for (final Composite subject : remainingComposites) {
            final Set<Component> allOtherParts = remainingComposites.stream()
                .filter(x -> !subject.equals(x))
                .map(Composite::parts)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
            final boolean isContainedInOthers = subject.parts()
                .stream()
                .allMatch(allOtherParts::contains);
            if (isContainedInOthers) {
                collectivelyContainedComposites.add(subject);
            }
        }

        final Set<Composite> actuallyContainedComposites = new HashSet<>();
        for (final Composite subject : collectivelyContainedComposites) {
            final Set<Component> allOtherParts = remainingComposites.stream()
                .filter(x -> !subject.equals(x))
                .filter(x -> !collectivelyContainedComposites.contains(x))
                .map(Composite::parts)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
            final boolean isContainedInOthers = subject.parts()
                .stream()
                .allMatch(allOtherParts::contains);
            if (isContainedInOthers) {
                actuallyContainedComposites.add(subject);
            }
        }

        remainingComposites.removeAll(actuallyContainedComposites);

        return remainingComposites;

    }

    private static Set<OperationInterface> collectVisibleProvisions(final Set<Component> components,
            final Set<Composite> composites) {
        // Collect globally visible provisions
        final Set<OperationInterface> provisions = new HashSet<>();
        // 1. Collect composite provisions
        composites.stream()
            .flatMap(x -> x.provisions()
                .stream())
            .forEach(provisions::add);
        // 2. Collect bare components
        final Set<Component> containedComponents = composites.stream()
            .flatMap(x -> x.parts()
                .stream())
            .collect(Collectors.toSet());
        final Set<Component> bareComponents = components.stream()
            .filter(x -> !containedComponents.contains(x))
            .collect(Collectors.toSet());
        // 3. Collect bare component provisions
        bareComponents.stream()
            .flatMap(x -> x.provisions()
                .getGrouped()
                .keySet()
                .stream())
            .forEach(provisions::add);

        return provisions;
    }

    private Map<OperationInterface, Set<Operation>> createOperationInterfaces() {
        final List<Map<OperationInterface, Set<Operation>>> constructedOperationInterfaces = this.getComponents()
            .stream()
            .map(x -> x.provisions()
                .simplified())
            .collect(Collectors.toList());
        this.getComponents()
            .stream()
            .map(x -> x.requirements()
                .simplified())
            .forEach(x -> constructedOperationInterfaces.add(x));
        this.getCompositeComponents()
            .stream()
            .flatMap(x -> x.provisions()
                .stream())
            .forEach(x -> constructedOperationInterfaces.add(x.simplified()));
        this.getCompositeComponents()
            .stream()
            .flatMap(x -> x.requirements()
                .stream())
            .forEach(x -> constructedOperationInterfaces.add(x.simplified()));
        return MapMerger.merge(constructedOperationInterfaces);
    }

    public Set<Component> getComponents() {
        return this.components;
    }

    public Set<Composite> getCompositeComponents() {
        return this.composites;
    }

    public Map<OperationInterface, Set<Operation>> getOperationInterfaces() {
        return this.operationInterfaces;
    }
}
