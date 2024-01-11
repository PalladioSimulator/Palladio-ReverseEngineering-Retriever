package org.palladiosimulator.somox.discoverer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.CommonPlugin;
import org.openjdk.nashorn.api.scripting.NashornException;
import org.openjdk.nashorn.api.tree.CompilationUnitTree;
import org.openjdk.nashorn.api.tree.Parser;
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.somox.analyzer.rules.engine.RuleEngineConfiguration;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class EcmaScriptDiscoverer implements Discoverer {

    private static final String DISCOVERER_ID = "org.palladiosimulator.somox.discoverer.ecmascript";

    @Override
    public IBlackboardInteractingJob<RuleEngineBlackboard> create(final RuleEngineConfiguration configuration,
            final RuleEngineBlackboard blackboard) {
        return new AbstractBlackboardInteractingJob<>() {

            @Override
            public void cleanup(final IProgressMonitor monitor) throws CleanupFailedException {
            }

            @Override
            public void execute(final IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
                final Path root = Paths.get(CommonPlugin.asLocalURI(configuration.getInputFolder())
                    .devicePath());
                setBlackboard(Objects.requireNonNull(blackboard));
                final Map<Path, CompilationUnitTree> compilationUnits = new HashMap<>();
                Stream.concat(Discoverer.find(root, ".js", logger), Discoverer.find(root, ".ts", logger))
                    .forEach(p -> {
                        try {
                            final CompilationUnitTree compilationUnit = Parser.create()
                                .parse(p.toFile(), d -> {
                                    System.out.println(d);
                                });
                            compilationUnits.put(p, compilationUnit);
                        } catch (NashornException | IOException e) {
                            logger.error(String.format("%s could not be read correctly.", p), e);
                        }
                    });
                getBlackboard().putDiscoveredFiles(DISCOVERER_ID, compilationUnits);
            }

            @Override
            public String getName() {
                return "ECMAScript Discoverer Job";
            }
        };
    }

    @Override
    public Set<String> getConfigurationKeys() {
        return Collections.emptySet();
    }

    @Override
    public String getID() {
        return DISCOVERER_ID;
    }

    @Override
    public String getName() {
        return "ECMAScript Discoverer";
    }
}
