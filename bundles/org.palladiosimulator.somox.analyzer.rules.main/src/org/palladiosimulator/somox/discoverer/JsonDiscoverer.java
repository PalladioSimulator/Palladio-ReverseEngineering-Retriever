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

import org.eclipse.core.runtime.IProgressMonitor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineConfiguration;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class JsonDiscoverer implements Discoverer {

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
                final Map<String, JSONObject> jsons = new HashMap<>();
                Discoverer.find(root, ".json", logger).forEach(p -> {
                    try (Reader reader = new FileReader(p)) {
                        jsons.put(p, (JSONObject) new JSONParser().parse(reader));
                    } catch (ClassCastException | IOException | ParseException e) {
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
