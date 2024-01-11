package org.palladiosimulator.retriever.extraction.discoverers;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.CommonPlugin;
import org.palladiosimulator.retriever.extraction.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.retriever.extraction.discoverers.wrappers.YamlMapper;
import org.palladiosimulator.retriever.extraction.engine.RuleEngineConfiguration;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class YamlDiscoverer implements Discoverer {

    public static final String DISCOVERER_ID = "org.palladiosimulator.somox.discoverer.yaml";
    public static final String MAPPER_PARTITION_KEY = DISCOVERER_ID + ".mappers";

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
                final Map<Path, Object> yamls = new HashMap<>();
                final Map<Path, YamlMapper> mappers = new HashMap<>();
                Stream.concat(Discoverer.find(root, ".yml", logger), Discoverer.find(root, ".yaml", logger))
                    .forEach(p -> {
                        try (Reader reader = new FileReader(p.toFile())) {
                            List<Object> yamlContents = new ArrayList<>();
                            new Yaml().loadAll(reader)
                                .forEach(yamlContents::add);
                            yamls.put(p, yamlContents);
                            mappers.put(p, new YamlMapper(yamlContents));
                        } catch (final IOException | YAMLException e) {
                            logger.error(String.format("%s could not be read correctly.", p), e);
                        }
                    });
                getBlackboard().putDiscoveredFiles(DISCOVERER_ID, yamls);
                getBlackboard().putDiscoveredFiles(MAPPER_PARTITION_KEY, mappers);
            }

            @Override
            public String getName() {
                return "YAML Discoverer Job";
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
        return "YAML Discoverer";
    }
}
