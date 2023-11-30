package org.palladiosimulator.somox.analyzer.rules.engine;

import java.nio.file.Path;

import org.eclipse.core.runtime.IProgressMonitor;
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

/**
 * This interface has to be implemented in order to write rules. The method will be used by the
 * RuleEngine class to process all written rule lines which are inside the method.
 */
public interface Rule extends Service {
    public abstract void processRules(RuleEngineBlackboard blackboard, Path path);

    public abstract boolean isBuildRule();

    @Override
    default IBlackboardInteractingJob<RuleEngineBlackboard> create(RuleEngineConfiguration configuration,
            RuleEngineBlackboard blackboard) {
        Rule rule = this;
        return new AbstractBlackboardInteractingJob<RuleEngineBlackboard>() {
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