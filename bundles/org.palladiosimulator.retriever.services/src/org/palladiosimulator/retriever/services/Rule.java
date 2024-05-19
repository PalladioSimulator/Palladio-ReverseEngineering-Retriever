package org.palladiosimulator.retriever.services;

import java.nio.file.Path;

import org.eclipse.core.runtime.IProgressMonitor;
import org.palladiosimulator.retriever.services.blackboard.RetrieverBlackboard;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

/**
 * This interface has to be implemented in order to write rules.
 */
public interface Rule extends Service {
    public abstract void processRules(RetrieverBlackboard blackboard, Path path);

    public abstract boolean isBuildRule();

    @Override
    default IBlackboardInteractingJob<RetrieverBlackboard> create(final RetrieverConfiguration configuration,
            final RetrieverBlackboard blackboard) {
        final Rule rule = this;
        return new AbstractBlackboardInteractingJob<>() {
            @Override
            public void execute(final IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
                for (final Path path : blackboard.getDiscoveredPaths()) {
                    rule.processRules(blackboard, path);
                }
            }

            @Override
            public void cleanup(final IProgressMonitor monitor) throws CleanupFailedException {
            }

            @Override
            public String getName() {
                return rule.getName() + " Job";
            }
        };
    }
}