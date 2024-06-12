package org.palladiosimulator.retriever.extraction.discoverers;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.CommonPlugin;
import org.palladiosimulator.retriever.services.Discoverer;
import org.palladiosimulator.retriever.services.RetrieverConfiguration;
import org.palladiosimulator.retriever.services.blackboard.RetrieverBlackboard;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class PropertiesDiscoverer implements Discoverer {

    public static final String DISCOVERER_ID = "org.palladiosimulator.retriever.extraction.discoverers.properties";

    @Override
    public IBlackboardInteractingJob<RetrieverBlackboard> create(final RetrieverConfiguration configuration,
            final RetrieverBlackboard blackboard) {
        return new AbstractBlackboardInteractingJob<>() {

            @Override
            public void cleanup(final IProgressMonitor monitor) throws CleanupFailedException {
            }

            @Override
            public void execute(final IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
                final Path root = Paths.get(CommonPlugin.asLocalURI(configuration.getInputFolder())
                    .devicePath());
                this.setBlackboard(Objects.requireNonNull(blackboard));
                final Map<Path, Object> propertyFiles = new HashMap<>();
                Discoverer.find(root, ".properties", this.logger)
                    .forEach(p -> {
                        try (Reader reader = new FileReader(p.toFile())) {
                            final Properties properties = new Properties();
                            properties.load(reader);
                            propertyFiles.put(p, properties);
                        } catch (final IOException | IllegalArgumentException e) {
                            this.logger.error(String.format("%s could not be read correctly.", p), e);
                        }
                    });
                this.getBlackboard()
                    .putDiscoveredFiles(DISCOVERER_ID, propertyFiles);
            }

            @Override
            public String getName() {
                return ".properties Discoverer Job";
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
        return ".properties Discoverer";
    }
}
