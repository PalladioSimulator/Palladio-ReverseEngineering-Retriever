package org.palladiosimulator.retriever.services.blackboard;

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

import de.uka.ipd.sdq.workflow.blackboard.Blackboard;

public class RetrieverBlackboard extends Blackboard<Object> {

    public static final String KEY_REPOSITORY = "org.palladiosimulator.retriever.repository";
    public static final String KEY_SEFF_ASSOCIATIONS = "org.palladiosimulator.retriever.seff_associations";

    private final Map<RepositoryComponent, CompilationUnit> repositoryComponentLocations;
    private final Map<Path, Set<CompilationUnit>> systemAssociations;
    private final Map<System, Path> systemPaths;
    private final Set<String> discovererIDs;
    private Object pcmDetector;

    public RetrieverBlackboard(final Object pcmDetector) {
        super();
        this.repositoryComponentLocations = new HashMap<>();
        this.systemAssociations = new HashMap<>();
        this.systemPaths = new HashMap<>();
        this.discovererIDs = new HashSet<>();
        this.pcmDetector = pcmDetector;
        this.addPartition(KEY_SEFF_ASSOCIATIONS, new HashMap<>());
    }

    public CompilationUnit putRepositoryComponentLocation(final RepositoryComponent repoComp,
            final CompilationUnit compilationUnit) {
        return this.repositoryComponentLocations.put(repoComp, compilationUnit);
    }

    public Map<RepositoryComponent, CompilationUnit> getRepositoryComponentLocations() {
        return Collections.unmodifiableMap(this.repositoryComponentLocations);
    }

    public void setPCMDetector(final Object pcmDetector) {
        this.pcmDetector = pcmDetector;
    }

    public Object getPCMDetector() {
        return this.pcmDetector;
    }

    public void addSystemAssociations(final Path path, final Set<CompilationUnit> compilationUnits) {
        this.systemAssociations.put(path, Collections.unmodifiableSet(compilationUnits));
    }

    public Map<Path, Set<CompilationUnit>> getSystemAssociations() {
        return Collections.unmodifiableMap(this.systemAssociations);
    }

    public void putSystemPath(final System system, final Path path) {
        this.systemPaths.put(system, path);
    }

    public void putSeffAssociation(final ASTNode astNode, final ServiceEffectSpecification seff) {
        @SuppressWarnings("unchecked")
        final Map<ASTNode, ServiceEffectSpecification> seffAssociations = (Map<ASTNode, ServiceEffectSpecification>) this
            .getPartition(KEY_SEFF_ASSOCIATIONS);
        seffAssociations.put(astNode, seff);
    }

    public ServiceEffectSpecification getSeffAssociation(final ASTNode astNode) {
        @SuppressWarnings("unchecked")
        final Map<ASTNode, ServiceEffectSpecification> seffAssociations = (Map<ASTNode, ServiceEffectSpecification>) this
            .getPartition(KEY_SEFF_ASSOCIATIONS);
        return seffAssociations.get(astNode);
    }

    public Map<ASTNode, ServiceEffectSpecification> getSeffAssociations() {
        @SuppressWarnings("unchecked")
        final Map<ASTNode, ServiceEffectSpecification> seffAssociations = (Map<ASTNode, ServiceEffectSpecification>) this
            .getPartition(KEY_SEFF_ASSOCIATIONS);
        return Collections.unmodifiableMap(seffAssociations);
    }

    public Map<System, Path> getSystemPaths() {
        return Collections.unmodifiableMap(this.systemPaths);
    }

    public <T> void putDiscoveredFiles(final String discovererID, final Map<Path, T> pathsToFiles) {
        this.discovererIDs.add(discovererID);
        this.addPartition(discovererID, pathsToFiles);
    }

    public <T> Map<Path, T> getDiscoveredFiles(final String discovererID, final Class<T> fileClass) {
        final Object partition = this.getPartition(discovererID);
        if (!(partition instanceof Map)) {
            return new HashMap<>();
        }
        @SuppressWarnings("unchecked") // Not unchecked.
        final Map<Object, Object> map = (Map<Object, Object>) partition;
        if (map.isEmpty()) {
            return new HashMap<>();
        }
        final boolean allEntriesHaveCorrectType = map.entrySet()
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
        final Set<Path> discoveredPaths = new HashSet<>();
        for (final String discovererID : this.discovererIDs) {
            @SuppressWarnings("unchecked") // Local data structure, this assumption is an invariant.
            final Map<Path, Object> partition = (Map<Path, Object>) this.getPartition(discovererID);
            discoveredPaths.addAll(partition.keySet());
        }
        return discoveredPaths;
    }
}
