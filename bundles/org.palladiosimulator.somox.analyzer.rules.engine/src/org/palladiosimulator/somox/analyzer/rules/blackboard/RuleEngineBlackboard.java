package org.palladiosimulator.somox.analyzer.rules.blackboard;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.palladiosimulator.pcm.core.entity.Entity;
import org.palladiosimulator.pcm.repository.RepositoryComponent;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.somox.analyzer.rules.engine.EMFTextPCMDetector;
import org.palladiosimulator.somox.analyzer.rules.engine.EclipsePCMDetector;

import com.google.common.collect.Sets;

import de.uka.ipd.sdq.workflow.blackboard.Blackboard;

public class RuleEngineBlackboard extends Blackboard<Object> {

    private Set<CompilationUnitWrapper> compilationUnits;
    private Map<CompilationUnitWrapper, Path> compilationUnitLocations;
    private Map<RepositoryComponent, CompilationUnitWrapper> repositoryComponentLocations;
    private Map<Entity, CompilationUnitWrapper> entityLocations;
    private Map<Path, Set<CompilationUnitWrapper>> systemAssociations;
    private Map<System, Path> systemPaths;
    private EMFTextPCMDetector emfTextPcmDetector;
    private EclipsePCMDetector eclipsePcmDetector;

    public RuleEngineBlackboard() {
        compilationUnits = new HashSet<>();
        compilationUnitLocations = new HashMap<>();
        repositoryComponentLocations = new HashMap<>();
        entityLocations = new HashMap<>();
        systemAssociations = new HashMap<>();
        systemPaths = new HashMap<>();
    }

    public Path putCompilationUnitLocation(CompilationUnitWrapper compilationUnit, Path path) {
        return compilationUnitLocations.put(compilationUnit, path);
    }

    public Path getCompilationUnitLocation(CompilationUnitWrapper compilationUnit) {
        return compilationUnitLocations.get(compilationUnit);
    }

    public CompilationUnitWrapper putRepositoryComponentLocation(RepositoryComponent repoComp,
            CompilationUnitWrapper compilationUnit) {
        entityLocations.put(repoComp, compilationUnit);
        return repositoryComponentLocations.put(repoComp, compilationUnit);
    }

    public Map<RepositoryComponent, CompilationUnitWrapper> getRepositoryComponentLocations() {
        return Collections.unmodifiableMap(repositoryComponentLocations);
    }

    public Map<Entity, Path> getEntityPaths() {
        final Map<Entity, Path> entityPaths = new HashMap<>();

        for (Entity entity : entityLocations.keySet()) {
            CompilationUnitWrapper compilationUnit = entityLocations.get(entity);
            if (compilationUnit == null)
                continue;
            Path path = compilationUnitLocations.get(compilationUnit);
            if (path == null)
                continue;
            entityPaths.put(entity, path);
        }

        for (Entry<System, Path> entry : systemPaths.entrySet()) {
            entityPaths.put(entry.getKey(), entry.getValue());
        }

        return Collections.unmodifiableMap(entityPaths);
    }

    public void setEMFTextPCMDetector(EMFTextPCMDetector pcmDetector) {
        this.emfTextPcmDetector = pcmDetector;
    }

    public EMFTextPCMDetector getEMFTextPCMDetector() {
        return this.emfTextPcmDetector;
    }

    public void setEclipsePCMDetector(EclipsePCMDetector pcmDetector) {
        this.eclipsePcmDetector = pcmDetector;
    }

    public EclipsePCMDetector getEclipsePCMDetector() {
        return this.eclipsePcmDetector;
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
    public Set<CompilationUnitWrapper> getCompilationUnitAt(Path path) {
        if (path == null) {
            // Return all registered CompilationUnits that are not associated with a path
            return Sets.difference(compilationUnits, compilationUnitLocations.keySet());
        }

        Set<CompilationUnitWrapper> compUnit = new HashSet<>();
        for (Entry<CompilationUnitWrapper, Path> entry : compilationUnitLocations.entrySet()) {
            // Path::equals is enough because the working directory does not change
            if (entry.getValue()
                .equals(path.normalize())) {
                compUnit.add(entry.getKey());
            }
        }

        return compUnit;
    }

    public void addCompilationUnit(CompilationUnitWrapper compilationUnit) {
        compilationUnits.add(compilationUnit);
    }

    public void addCompilationUnits(Collection<CompilationUnitWrapper> compilationUnits) {
        this.compilationUnits.addAll(compilationUnits);
    }

    public Set<CompilationUnitWrapper> getCompilationUnits() {
        return Collections.unmodifiableSet(compilationUnits);
    }

    public void addSystemAssociations(Path path, Set<CompilationUnitWrapper> compilationUnits) {
        systemAssociations.put(path, Collections.unmodifiableSet(compilationUnits));
    }

    public Map<Path, Set<CompilationUnitWrapper>> getSystemAssociations() {
        return Collections.unmodifiableMap(systemAssociations);
    }

    public void putSystemPath(System system, Path path) {
        systemPaths.put(system, path);
    }
}
