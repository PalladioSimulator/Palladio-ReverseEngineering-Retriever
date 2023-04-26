package org.palladiosimulator.somox.analyzer.rules.blackboard;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.palladiosimulator.pcm.core.entity.Entity;
import org.palladiosimulator.pcm.repository.RepositoryComponent;
import org.palladiosimulator.pcm.seff.ServiceEffectSpecification;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.somox.analyzer.rules.engine.PCMDetector;

import com.google.common.collect.Sets;

import de.uka.ipd.sdq.workflow.blackboard.Blackboard;

public class RuleEngineBlackboard extends Blackboard<Object> {

    public static final String KEY_REPOSITORY = "org.palladiosimulator.somox.analyzer.repository";
    public static final String KEY_SEFF_ASSOCIATIONS = "org.palladiosimulator.somox.analyzer.seff_associations";

    private final Set<CompilationUnit> compilationUnits;
    private final Map<CompilationUnit, Path> compilationUnitLocations;
    private final Map<RepositoryComponent, CompilationUnit> repositoryComponentLocations;
    private final Map<Entity, CompilationUnit> entityLocations;
    private final Map<Path, Set<CompilationUnit>> systemAssociations;
    private final Map<System, Path> systemPaths;
    private PCMDetector pcmDetector;

    public RuleEngineBlackboard() {
        compilationUnits = new HashSet<>();
        compilationUnitLocations = new HashMap<>();
        repositoryComponentLocations = new HashMap<>();
        entityLocations = new HashMap<>();
        systemAssociations = new HashMap<>();
        systemPaths = new HashMap<>();
        addPartition(KEY_SEFF_ASSOCIATIONS, new HashMap<>());
    }

    public Path putCompilationUnitLocation(CompilationUnit compilationUnit, Path path) {
        return compilationUnitLocations.put(compilationUnit, path);
    }

    public Path getCompilationUnitLocation(CompilationUnit compilationUnit) {
        return compilationUnitLocations.get(compilationUnit);
    }

    public CompilationUnit putRepositoryComponentLocation(RepositoryComponent repoComp,
            CompilationUnit compilationUnit) {
        entityLocations.put(repoComp, compilationUnit);
        return repositoryComponentLocations.put(repoComp, compilationUnit);
    }

    public Map<RepositoryComponent, CompilationUnit> getRepositoryComponentLocations() {
        return Collections.unmodifiableMap(repositoryComponentLocations);
    }

    public Map<Entity, Path> getEntityPaths() {
        final Map<Entity, Path> entityPaths = new HashMap<>();

        for (Entity entity : entityLocations.keySet()) {
            CompilationUnit compilationUnit = entityLocations.get(entity);
            if (compilationUnit == null) {
                continue;
            }
            Path path = compilationUnitLocations.get(compilationUnit);
            if (path == null) {
                continue;
            }
            entityPaths.put(entity, path);
        }

        for (Entry<System, Path> entry : systemPaths.entrySet()) {
            entityPaths.put(entry.getKey(), entry.getValue());
        }

        return Collections.unmodifiableMap(entityPaths);
    }

    public void setPCMDetector(PCMDetector pcmDetector) {
        this.pcmDetector = pcmDetector;
    }

    public PCMDetector getPCMDetector() {
        return pcmDetector;
    }

    /**
     * Provides all CompilationUnits found at the specified path. For local files, this should
     * usually only be a single file. If {@code path == null}, all CompilationUnits that do not have
     * a path associated with them (e.g. standard library classes or other library classes) are
     * returned.
     *
     * @param path
     *            the path to look for registered CompilationUnits at
     * @return the CompilationUnits or {@code null} if there was none at the {@code path}
     */
    public Set<CompilationUnit> getCompilationUnitAt(Path path) {
        if (path == null) {
            // Return all registered CompilationUnits that are not associated with a path
            return Sets.difference(compilationUnits, compilationUnitLocations.keySet());
        }

        Set<CompilationUnit> compUnit = new HashSet<>();
        for (Entry<CompilationUnit, Path> entry : compilationUnitLocations.entrySet()) {
            // Path::equals is enough because the working directory does not change
            if (entry.getValue()
                .equals(path.normalize())) {
                compUnit.add(entry.getKey());
            }
        }

        return compUnit;
    }

    public void addCompilationUnit(CompilationUnit compilationUnit) {
        compilationUnits.add(compilationUnit);
    }

    public void addCompilationUnits(Collection<CompilationUnit> compilationUnits) {
        this.compilationUnits.addAll(compilationUnits);
    }

    public Set<CompilationUnit> getCompilationUnits() {
        return Collections.unmodifiableSet(compilationUnits);
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
}
