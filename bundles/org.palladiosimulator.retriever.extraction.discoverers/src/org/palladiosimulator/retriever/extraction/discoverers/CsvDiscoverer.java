package org.palladiosimulator.retriever.extraction.discoverers;

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
import org.palladiosimulator.retriever.extraction.blackboard.RetrieverBlackboard;
import org.palladiosimulator.retriever.extraction.engine.Discoverer;
import org.palladiosimulator.retriever.extraction.engine.RetrieverConfiguration;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class CsvDiscoverer implements Discoverer {

    private static final String DISCOVERER_ID = "org.palladiosimulator.retriever.extraction.discoverers.csv";

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
                final Map<Path, List<CSVRecord>> csvs = new HashMap<>();
                Discoverer.find(root, ".csv", this.logger)
                    .forEach(p -> {
                        final List<CSVRecord> records = new LinkedList<>();
                        try (Reader reader = new FileReader(p.toFile())) {
                            DEFAULT.parse(reader)
                                .forEach(records::add);
                        } catch (final IllegalStateException | IOException e) {
                            this.logger.error(String.format("%s could not be read correctly.", p), e);
                        }
                        csvs.put(p, records);
                    });
                this.getBlackboard()
                    .putDiscoveredFiles(DISCOVERER_ID, csvs);
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
