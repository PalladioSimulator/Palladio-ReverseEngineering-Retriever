package org.palladiosimulator.somox.analyzer.rules.blackboard;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.emftext.language.java.containers.impl.CompilationUnitImpl;
import org.palladiosimulator.pcm.core.entity.Entity;
import org.somox.extractor.ExtractionResult;
import org.somox.gast2seff.jobs.SoMoXBlackboard;

public class RuleEngineBlackboard extends SoMoXBlackboard {

    private Map<String, ExtractionResult> extractionResults;
    private Map<CompilationUnitImpl, Set<Path>> compilationUnitLocations;
    private Map<Entity, CompilationUnitImpl> entityLocations;

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
}
