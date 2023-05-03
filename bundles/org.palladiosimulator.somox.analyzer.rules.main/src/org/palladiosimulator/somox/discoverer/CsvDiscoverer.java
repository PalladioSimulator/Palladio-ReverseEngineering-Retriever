package org.palladiosimulator.somox.discoverer;

import static org.apache.commons.csv.CSVFormat.DEFAULT;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.csv.CSVRecord;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.CommonPlugin;
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineConfiguration;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class CsvDiscoverer implements Discoverer {

    private static final String DISCOVERER_ID = "org.palladiosimulator.somox.discoverer.csv";

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
                final Map<String, List<CSVRecord>> csvs = new HashMap<>();
                Discoverer.find(root, ".csv", logger)
                    .forEach(p -> {
                        final List<CSVRecord> records = new LinkedList<>();
                        try (Reader reader = new FileReader(p)) {
                            DEFAULT.parse(reader)
                                .forEach(records::add);
                        } catch (final IllegalStateException | IOException e) {
                            logger.error(String.format("%s could not be read correctly.", p), e);
                        }
                        csvs.put(p, records);
                    });
                getBlackboard().addPartition(DISCOVERER_ID, csvs);
            }

            @Override
            public String getName() {
                return "CSV Discoverer Job";
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
        return "CSV Discoverer";
    }
}
