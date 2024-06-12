package org.palladiosimulator.retriever.extraction.discoverers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.CommonPlugin;
import org.json.JSONException;
import org.json.JSONObject;
import org.palladiosimulator.retriever.services.Discoverer;
import org.palladiosimulator.retriever.services.RetrieverConfiguration;
import org.palladiosimulator.retriever.services.blackboard.RetrieverBlackboard;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class JsonDiscoverer implements Discoverer {

    public static final String DISCOVERER_ID = "org.palladiosimulator.retriever.extraction.discoverers.json";

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
                final Map<Path, JSONObject> jsons = new HashMap<>();
                Discoverer.find(root, ".json", this.logger)
                    .forEach(p -> {
                        try (BufferedReader reader = new BufferedReader(new FileReader(p.toFile()))) {
                            final String jsonSource = reader.lines()
                                .collect(Collectors.joining(System.lineSeparator()));
                            jsons.put(p, new JSONObject(jsonSource));
                        } catch (ClassCastException | IOException | JSONException e) {
                            this.logger.error(String.format("%s could not be read correctly.", p), e);
                        }
                    });
                this.getBlackboard()
                    .putDiscoveredFiles(DISCOVERER_ID, jsons);
            }

            @Override
            public String getName() {
                return "JSON Discoverer Job";
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
        return "JSON Discoverer";
    }
}
