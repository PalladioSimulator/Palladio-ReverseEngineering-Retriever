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
import java.util.Set;

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
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

public class SqlDiscoverer implements Discoverer {

    public static final String DISCOVERER_ID = "org.palladiosimulator.retriever.extraction.discoverers.sql";

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
                setBlackboard(Objects.requireNonNull(blackboard));
                final Map<Path, Statement> sqls = new HashMap<>();
                Discoverer.find(root, ".sql", logger)
                    .forEach(p -> {
                        try (Reader reader = new FileReader(p.toFile())) {
                            sqls.put(p, CCJSqlParserUtil.parse(reader));
                        } catch (final IOException | JSQLParserException e) {
                            logger.error(String.format("%s could not be read correctly.", p), e);
                        }
                    });
                getBlackboard().putDiscoveredFiles(DISCOVERER_ID, sqls);
            }

            @Override
            public String getName() {
                return "SQL Discoverer Job";
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
        return "SQL Discoverer";
    }
}
