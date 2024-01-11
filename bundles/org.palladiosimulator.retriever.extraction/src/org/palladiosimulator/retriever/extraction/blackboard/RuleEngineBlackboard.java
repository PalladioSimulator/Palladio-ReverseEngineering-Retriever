package org.palladiosimulator.retriever.extraction.blackboard;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.palladiosimulator.pcm.repository.RepositoryComponent;
import org.palladiosimulator.pcm.seff.ServiceEffectSpecification;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.retriever.extraction.engine.PCMDetector;

import de.uka.ipd.sdq.workflow.blackboard.Blackboard;

public class RuleEngineBlackboard extends Blackboard<Object> {

    public static final String KEY_REPOSITORY = "org.palladiosimulator.somox.analyzer.repository";
    public static final String KEY_SEFF_ASSOCIATIONS = "org.palladiosimulator.somox.analyzer.seff_associations";

    private final Map<RepositoryComponent, CompilationUnit> repositoryComponentLocations;
    private final Map<Path, Set<CompilationUnit>> systemAssociations;
    private final Map<System, Path> systemPaths;
    private final Set<String> discovererIDs;
    private PCMDetector pcmDetector;

    public RuleEngineBlackboard() {
        super();
        repositoryComponentLocations = new HashMap<>();
        systemAssociations = new HashMap<>();
        systemPaths = new HashMap<>();
        discovererIDs = new HashSet<>();
        pcmDetector = new PCMDetector();
        addPartition(KEY_SEFF_ASSOCIATIONS, new HashMap<>());
    }

    public CompilationUnit putRepositoryComponentLocation(RepositoryComponent repoComp,
            CompilationUnit compilationUnit) {
        return repositoryComponentLocations.put(repoComp, compilationUnit);
    }

    public Map<RepositoryComponent, CompilationUnit> getRepositoryComponentLocations() {
        return Collections.unmodifiableMap(repositoryComponentLocations);
    }

    public void setPCMDetector(PCMDetector pcmDetector) {
        this.pcmDetector = pcmDetector;
    }

    public PCMDetector getPCMDetector() {
        return pcmDetector;
    }

    public void addSystemAssociations(Path path, Set<CompilationUnit> compilationUnits) {
        systemAssociations.put(path, Collections.unmodifiableSet(compilationUnits));
    }

    public Map<Path, Set<CompilationUnit>> getSystemAssociations() {
        return Collections.unmodifiableMap(systemAssociations);
    }

    public void putSystemPath(System system, Path path) {
        systemPaths.put(system, path);
    }

    public void putSeffAssociation(ASTNode astNode, ServiceEffectSpecification seff) {
        @SuppressWarnings("unchecked")
        Map<ASTNode, ServiceEffectSpecification> seffAssociations = (Map<ASTNode, ServiceEffectSpecification>) getPartition(
                KEY_SEFF_ASSOCIATIONS);
        seffAssociations.put(astNode, seff);
    }

    public ServiceEffectSpecification getSeffAssociation(ASTNode astNode) {
        @SuppressWarnings("unchecked")
        Map<ASTNode, ServiceEffectSpecification> seffAssociations = (Map<ASTNode, ServiceEffectSpecification>) getPartition(
                KEY_SEFF_ASSOCIATIONS);
        return seffAssociations.get(astNode);
    }

    public Map<ASTNode, ServiceEffectSpecification> getSeffAssociations() {
        @SuppressWarnings("unchecked")
        Map<ASTNode, ServiceEffectSpecification> seffAssociations = (Map<ASTNode, ServiceEffectSpecification>) getPartition(
                KEY_SEFF_ASSOCIATIONS);
        return Collections.unmodifiableMap(seffAssociations);
    }

    public <T> void putDiscoveredFiles(String discovererID, Map<Path, T> pathsToFiles) {
        discovererIDs.add(discovererID);
        addPartition(discovererID, pathsToFiles);
    }

    public <T> Map<Path, T> getDiscoveredFiles(String discovererID, Class<T> fileClass) {
        Object partition = getPartition(discovererID);
        if (!(partition instanceof Map)) {
            return new HashMap<>();
        }
        @SuppressWarnings("unchecked") // Not unchecked.
        Map<Object, Object> map = (Map<Object, Object>) partition;
        if (map.isEmpty()) {
            return new HashMap<>();
        }
        boolean allEntriesHaveCorrectType = map.entrySet()
            .stream()
            .allMatch(entry -> entry.getKey() instanceof Path && fileClass.isInstance(entry.getValue()));
        if (!allEntriesHaveCorrectType) {
            return new HashMap<>();
        }
        return map.entrySet()
            .stream()
            .collect(Collectors.toMap(entry -> (Path) entry.getKey(), entry -> fileClass.cast(entry.getValue())));
    }

    public Set<Path> getDiscoveredPaths() {
        Set<Path> discoveredPaths = new HashSet<>();
        for (String discovererID : discovererIDs) {
            @SuppressWarnings("unchecked") // Local data structure, this assumption is an invariant.
            Map<Path, Object> partition = (Map<Path, Object>) getPartition(discovererID);
            discoveredPaths.addAll(partition.keySet());
        }
        return discoveredPaths;
    }
}
