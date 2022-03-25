package org.palladiosimulator.somox.discoverer;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineConfiguration;
import org.yaml.snakeyaml.Yaml;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class YamlDiscoverer implements Discoverer {

    @Override
    public IBlackboardInteractingJob<RuleEngineBlackboard> create(final RuleEngineConfiguration configuration,
            final RuleEngineBlackboard blackboard) {
        return new AbstractBlackboardInteractingJob<>() {

            @Override
            public void cleanup(final IProgressMonitor monitor) throws CleanupFailedException {
                // TODO Auto-generated method stub
            }

            @Override
            public void execute(final IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
                final Path root = Paths.get(configuration.getInputFolder().devicePath()).toAbsolutePath().normalize();
                setBlackboard(Objects.requireNonNull(blackboard));
                final Map<String, Map<String, Object>> yamls = new HashMap<>();
                Stream.concat(Discoverer.find(root, ".yml", logger), Discoverer.find(root, ".yaml", logger)).forEach(p -> {
                    try (Reader reader = new FileReader(p)) {
                        yamls.put(p, new Yaml().load(reader));
                    } catch (final IOException e) {
                        logger.error(String.format("%s could not be read correctly.", p), e);
                    }
                });
            }

            @Override
            public String getName() {
                // TODO Auto-generated method stub
                return null;
            }
        };
    }

    @Override
    public Set<String> getConfigurationKeys() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getID() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }
}
