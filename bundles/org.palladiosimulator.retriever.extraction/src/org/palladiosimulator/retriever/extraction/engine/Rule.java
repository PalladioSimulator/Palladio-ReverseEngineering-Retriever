package org.palladiosimulator.retriever.extraction.engine;

import java.nio.file.Path;

import org.eclipse.core.runtime.IProgressMonitor;
import org.palladiosimulator.retriever.extraction.blackboard.RetrieverBlackboard;

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
    default IBlackboardInteractingJob<RetrieverBlackboard> create(RetrieverConfiguration configuration,
            RetrieverBlackboard blackboard) {
        Rule rule = this;
        return new AbstractBlackboardInteractingJob<RetrieverBlackboard>() {
            @Override
            public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
                for (Path path : blackboard.getDiscoveredPaths()) {
                    rule.processRules(blackboard, path);
                }
            }

            @Override
            public void cleanup(IProgressMonitor monitor) throws CleanupFailedException {
            }

            @Override
            public String getName() {
                return rule.getName() + " Job";
            }
        };
    }
}