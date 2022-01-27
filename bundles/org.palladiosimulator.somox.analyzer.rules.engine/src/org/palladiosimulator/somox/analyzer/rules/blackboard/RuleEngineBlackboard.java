package org.palladiosimulator.somox.analyzer.rules.blackboard;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.emftext.language.java.containers.impl.CompilationUnitImpl;
import org.palladiosimulator.pcm.core.entity.Entity;
import org.palladiosimulator.somox.analyzer.rules.engine.PCMDetectorSimple;
import org.somox.extractor.ExtractionResult;
import org.somox.gast2seff.jobs.SoMoXBlackboard;

public class RuleEngineBlackboard extends SoMoXBlackboard {

    private static final Logger LOG = Logger.getLogger(RuleEngineBlackboard.class);

    private Map<String, ExtractionResult> extractionResults;
    private Map<CompilationUnitImpl, Set<Path>> compilationUnitLocations;
    private Map<Entity, CompilationUnitImpl> entityLocations;
    private PCMDetectorSimple pcmDetector;

    public RuleEngineBlackboard() {
        extractionResults = new HashMap<>();
        compilationUnitLocations = new HashMap<>();
        entityLocations = new HashMap<>();
    }

    public ExtractionResult putExtractionResult(String identifier, ExtractionResult extractionResult) {
        return extractionResults.put(identifier, extractionResult);
    }

    public Map<String, ExtractionResult> getExtractionResults() {
        return Collections.unmodifiableMap(extractionResults);
    }

    public void addCompilationUnitLocation(CompilationUnitImpl compilationUnit, Path path) {
        Set<Path> paths = compilationUnitLocations.get(compilationUnit);
        if (paths == null) {
            paths = new HashSet<>();
            compilationUnitLocations.put(compilationUnit, paths);
        }
        paths.add(path);
    }
    
    public Set<Path> getCompilationUnitLocations(CompilationUnitImpl compilationUnit) {
        Set<Path> paths = compilationUnitLocations.get(compilationUnit);
        if (paths == null) {
            return null;
        } else {
            return Collections.unmodifiableSet(paths);
        }
    }

    public CompilationUnitImpl putEntityLocation(Entity entity, CompilationUnitImpl compilationUnit) {
        return entityLocations.put(entity, compilationUnit);
    }

    public Map<Entity, Set<Path>> getEntityPaths() {
        final Map<Entity, Set<Path>> entityPaths = new HashMap<>();

        for (Entity entity : entityLocations.keySet()) {
            CompilationUnitImpl compilationUnit = entityLocations.get(entity);
            if (compilationUnit == null)
                continue;
            Set<Path> path = compilationUnitLocations.get(compilationUnit);
            if (path == null)
                continue;
            entityPaths.put(entity, path);
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
     * Provides the CompilationUnit found at the specified path.
     * 
     * @param path the path to look for a registered CompilationUnit at
     * @return the CompilationUnit or {@code null} if there was none at the {@code path}
     */
    public CompilationUnitImpl getCompilationUnitAt(Path path) {
        CompilationUnitImpl compUnit = null;

        for (Entry<CompilationUnitImpl, Set<Path>> entry : compilationUnitLocations.entrySet()) {
            // TODO is Path::equals enough?
            if (entry.getValue().contains(path)) {
                if (compUnit == null) {
                    compUnit = entry.getKey();
                } else {
                    LOG.warn(String.format("Unexpected ambiguity: Path %s is both associated with at least two "
                            + "CompilationUnits: \"%s\" and \"%s\"",
                            path, compUnit.getName(), entry.getKey().getName()));
                }
            }
        }

        return compUnit;
    }
}
