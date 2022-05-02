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
import org.palladiosimulator.somox.analyzer.rules.engine.PCMDetectorSimple;
import org.somox.extractor.ExtractionResult;
import org.somox.gast2seff.jobs.SoMoXBlackboard;

import com.google.common.collect.Sets;

public class RuleEngineBlackboard extends SoMoXBlackboard {

    private Map<String, ExtractionResult> extractionResults;
    private Set<CompilationUnitWrapper> compilationUnits;
    private Map<CompilationUnitWrapper, Set<Path>> compilationUnitLocations;
    private Map<RepositoryComponent, CompilationUnitWrapper> repositoryComponentLocations;
    private Map<Entity, CompilationUnitWrapper> entityLocations;
    private Map<Path, Set<CompilationUnitWrapper>> systemAssociations;
    private Map<System, Path> systemPaths;
    private PCMDetectorSimple pcmDetector;

    public RuleEngineBlackboard() {
        extractionResults = new HashMap<>();
        compilationUnits = new HashSet<>();
        compilationUnitLocations = new HashMap<>();
        repositoryComponentLocations = new HashMap<>();
        entityLocations = new HashMap<>();
        systemAssociations = new HashMap<>();
        systemPaths = new HashMap<>();
    }

    public ExtractionResult putExtractionResult(String identifier, ExtractionResult extractionResult) {
        return extractionResults.put(identifier, extractionResult);
    }

    public Map<String, ExtractionResult> getExtractionResults() {
        return Collections.unmodifiableMap(extractionResults);
    }

    public void addCompilationUnitLocation(CompilationUnitWrapper compilationUnit, Path path) {
        Set<Path> paths = compilationUnitLocations.get(compilationUnit);
        if (paths == null) {
            paths = new HashSet<>();
            compilationUnitLocations.put(compilationUnit, paths);
        }
        paths.add(path.normalize());
    }

    public Set<Path> getCompilationUnitLocations(CompilationUnitWrapper compilationUnit) {
        Set<Path> paths = compilationUnitLocations.get(compilationUnit);
        if (paths == null) {
            return Collections.emptySet();
        } else {
            return Collections.unmodifiableSet(paths);
        }
    }

    public CompilationUnitWrapper putRepositoryComponentLocation(RepositoryComponent repoComp,
            CompilationUnitWrapper compilationUnit) {
        entityLocations.put(repoComp, compilationUnit);
        return repositoryComponentLocations.put(repoComp, compilationUnit);
    }

    public Map<RepositoryComponent, CompilationUnitWrapper> getRepositoryComponentLocations() {
        return Collections.unmodifiableMap(repositoryComponentLocations);
    }

    public Map<Entity, Set<Path>> getEntityPaths() {
        final Map<Entity, Set<Path>> entityPaths = new HashMap<>();

        for (Entity entity : entityLocations.keySet()) {
            CompilationUnitWrapper compilationUnit = entityLocations.get(entity);
            if (compilationUnit == null)
                continue;
            Set<Path> path = compilationUnitLocations.get(compilationUnit);
            if (path == null)
                continue;
            entityPaths.put(entity, path);
        }

        for (Entry<System, Path> entry : systemPaths.entrySet()) {
            entityPaths.put(entry.getKey(), Set.of(entry.getValue()));
        }

        return Collections.unmodifiableMap(entityPaths);
    }

    public void setPCMDetector(PCMDetectorSimple pcmDetector) {
        this.pcmDetector = pcmDetector;
    }

    public PCMDetectorSimple getPCMDetector() {
        return this.pcmDetector;
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
        for (Entry<CompilationUnitWrapper, Set<Path>> entry : compilationUnitLocations.entrySet()) {
            // Path::equals is enough because the working directory does not change
            if (entry.getValue()
                .contains(path.normalize())) {
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
