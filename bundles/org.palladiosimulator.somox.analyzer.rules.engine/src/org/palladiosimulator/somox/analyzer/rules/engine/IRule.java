package org.palladiosimulator.somox.analyzer.rules.engine;

import java.nio.file.Path;

import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;

import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;

/**
 * This interface has to be implemented in order to write rules. The method will be used by the
 * RuleEngine class to process all written rule lines which are inside the method.
 */
public interface IRule extends Service {
    public abstract boolean processRules(RuleEngineBlackboard blackboard, Path path);

    public abstract boolean isBuildRule();

    @Override
    default IBlackboardInteractingJob<RuleEngineBlackboard> create(RuleEngineConfiguration configuration,
            RuleEngineBlackboard blackboard) {
        // TODO!
        throw new UnsupportedOperationException("TODO: Implement rules as jobs");
    }
}