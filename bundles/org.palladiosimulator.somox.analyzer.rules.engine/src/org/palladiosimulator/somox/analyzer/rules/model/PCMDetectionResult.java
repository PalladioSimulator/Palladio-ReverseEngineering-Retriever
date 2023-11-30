package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.palladiosimulator.somox.analyzer.rules.engine.MapMerger;

public class PCMDetectionResult {
    private final Set<Component> components;
    private final Set<Composite> composites;
    private final Map<OperationInterface, List<Operation>> operationInterfaces;

    public PCMDetectionResult(Map<CompUnitOrName, ComponentBuilder> components,
            Map<String, CompositeBuilder> composites, ProvisionsBuilder compositeProvisions,
            RequirementsBuilder compositeRequirements) {

        Map<CompUnitOrName, ComponentBuilder> freeComponents = PCMDetectionResult.filterFreeComponents(components,
                composites.values());

        // Collect globally visible provisions
        Set<Component> temporaryComponents = PCMDetectionResult.createComponents(components, compositeProvisions,
                compositeRequirements, Set.of());
        Set<Composite> temporaryComposites = PCMDetectionResult.createCompositeComponents(temporaryComponents,
                composites, compositeProvisions, compositeRequirements, Set.of());
        Set<OperationInterface> visibleProvisions = PCMDetectionResult.collectVisibleProvisions(temporaryComponents,
                temporaryComposites);

        PCMDetectionResult.removeBoundComponents(components, temporaryComposites);

        // TODO: Do not rebuild everything, that is theoretically not necessary since provisions do
        // not change.

        // Construct final result
        this.components = PCMDetectionResult.createComponents(freeComponents, compositeProvisions,
                compositeRequirements, visibleProvisions);
        this.composites = PCMDetectionResult.createCompositeComponents(this.components, composites, compositeProvisions,
                compositeRequirements, visibleProvisions);
        this.operationInterfaces = createOperationInterfaces();
    }

    private static Map<CompUnitOrName, ComponentBuilder> filterFreeComponents(
            Map<CompUnitOrName, ComponentBuilder> components, Collection<CompositeBuilder> composites) {
        Set<CompUnitOrName> boundComponents = new HashSet<>();
        for (CompUnitOrName identifier : components.keySet()) {
            if (composites.stream()
                .anyMatch(composite -> composite.hasPart(identifier))) {
                boundComponents.add(identifier);
            }
        }
        return components.entrySet()
            .stream()
            .filter(entry -> !boundComponents.contains(entry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Set<Component> createComponents(Map<CompUnitOrName, ComponentBuilder> components,
            ProvisionsBuilder compositeProvisions, RequirementsBuilder compositeRequirements,
            Set<OperationInterface> visibleProvisions) {
        List<OperationInterface> allDependencies = new LinkedList<>();
        // TODO: Aren't the dependencies of free components missing here? Is that alright?
        allDependencies.addAll(compositeRequirements.toList());
        allDependencies.addAll(compositeProvisions.toList());

        return components.values()
            .stream()
            .map(x -> x.create(allDependencies, visibleProvisions))
            .collect(Collectors.toSet());
    }

    private static Set<Composite> createCompositeComponents(Set<Component> freeComponents,
            Map<String, CompositeBuilder> composites, ProvisionsBuilder compositeProvisions,
            RequirementsBuilder compositeRequirements, Set<OperationInterface> visibleProvisions) {

        // Construct composites.
        Set<Composite> constructedComposites = new HashSet<>();
        List<Composite> allComposites = composites.values()
            .stream()
            .map(x -> x.construct(freeComponents, compositeRequirements.create(List.of(), visibleProvisions),
                    compositeProvisions.create(List.of()), visibleProvisions))
            .collect(Collectors.toList());

        // Remove redundant composites.
        Set<Composite> redundantComposites = new HashSet<>();
        for (int i = 0; i < allComposites.size(); ++i) {
            Composite subject = allComposites.get(i);
            long subsetCount = allComposites.subList(i + 1, allComposites.size())
                .stream()
                .filter(x -> subject.isSubsetOf(x) || x.isSubsetOf(subject))
                .count();

            // Any composite is guaranteed to be the subset of at least one composite in the
            // list,
            // namely itself. If it is the subset of any composites other than itself, it is
            // redundant.
            if (subsetCount > 0) {
                redundantComposites.add(subject);
            }

            // TODO: Is there any merging necessary, like adapting the redundant composite's
            // requirements to its peer?
            constructedComposites = allComposites.stream()
                .filter(x -> !redundantComposites.contains(x))
                .collect(Collectors.toUnmodifiableSet());
        }

        return constructedComposites;
    }

    private static Set<OperationInterface> collectVisibleProvisions(Set<Component> components,
            Set<Composite> composites) {
        // Collect globally visible provisions
        Set<OperationInterface> provisions = new HashSet<>();
        // 1. Collect composite provisions
        composites.stream()
            .flatMap(x -> x.provisions()
                .stream())
            .forEach(provisions::add);
        // 2. Collect bare components
        Set<Component> containedComponents = composites.stream()
            .flatMap(x -> x.parts()
                .stream())
            .collect(Collectors.toSet());
        Set<Component> bareComponents = components.stream()
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

    private static void removeBoundComponents(Map<CompUnitOrName, ComponentBuilder> freeComponents,
            Set<Composite> composites) {
        Set<CompUnitOrName> boundComponents = composites.stream()
            .flatMap(composite -> composite.parts()
                .stream())
            .map(part -> part.identifier())
            .collect(Collectors.toSet());
        for (CompUnitOrName identifier : boundComponents) {
            freeComponents.remove(identifier);
        }
    }

    private Map<OperationInterface, List<Operation>> createOperationInterfaces() {
        // TODO: This has to include composite interfaces as well
        List<Map<OperationInterface, List<Operation>>> constructedOperationInterfaces = getComponents().stream()
            .map(x -> x.provisions()
                .simplified())
            .collect(Collectors.toList());
        getComponents().stream()
            .map(x -> x.requirements()
                .simplified())
            .forEach(x -> constructedOperationInterfaces.add(x));
        getCompositeComponents().stream()
            .flatMap(x -> x.provisions()
                .stream())
            .forEach(x -> constructedOperationInterfaces.add(x.simplified()));
        getCompositeComponents().stream()
            .flatMap(x -> x.requirements()
                .stream())
            .forEach(x -> constructedOperationInterfaces.add(x.simplified()));
        return MapMerger.merge(constructedOperationInterfaces);
    }

    public Set<Component> getComponents() {
        return components;
    }

    public Set<Composite> getCompositeComponents() {
        return composites;
    }

    public Map<OperationInterface, List<Operation>> getOperationInterfaces() {
        return operationInterfaces;
    }
}
