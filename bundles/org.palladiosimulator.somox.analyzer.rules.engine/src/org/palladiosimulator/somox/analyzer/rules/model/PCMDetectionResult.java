package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.ArrayList;
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
        this.components = createComponents(components, compositeProvisions, compositeRequirements);
        this.composites = createCompositeComponents(composites, compositeProvisions, compositeRequirements);
        simplifyDependencies();
        this.operationInterfaces = createOperationInterfaces();
    }

    private Set<Component> createComponents(Map<CompUnitOrName, ComponentBuilder> components,
            ProvisionsBuilder compositeProvisions, RequirementsBuilder compositeRequirements) {
        List<OperationInterface> allDependencies = new LinkedList<>();
        // TODO: Aren't the dependencies of bare components missing here? Is that alright?
        allDependencies.addAll(compositeRequirements.toList());
        allDependencies.addAll(compositeProvisions.toList());

        return components.values()
            .stream()
            .map(x -> x.create(allDependencies))
            .collect(Collectors.toSet());
    }

    private Set<Composite> createCompositeComponents(Map<String, CompositeBuilder> composites,
            ProvisionsBuilder compositeProvisions, RequirementsBuilder compositeRequirements) {

        // Construct composites.
        Set<Composite> constructedComposites = new HashSet<>();
        List<Composite> allComposites = composites.values()
            .stream()
            .map(x -> x.construct(getComponents(), compositeRequirements.create(List.of()),
                    compositeProvisions.create(List.of())))
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

    private void simplifyDependencies() {
        // TODO: Collect globally visible provisions
        List<OperationInterface> provisions = new ArrayList<>();
        // 1. Collect composite provisions
        // 2. Collect bare components
        // 3. Collect bare component provisions

        // TODO: Generalize all requirements to the most specific provision
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
