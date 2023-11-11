package org.palladiosimulator.somox.analyzer.rules.engine;

import java.util.Set;

import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;

import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;

/**
 * The defining interface for any plug-in style service for the rule engine.
 *
 * @author Florian Bossert
 */
public interface Service {
    IBlackboardInteractingJob<RuleEngineBlackboard> create(RuleEngineConfiguration configuration,
            RuleEngineBlackboard blackboard);

    Set<String> getConfigurationKeys();

    String getName();

    String getID();

	Set<String> getRequiredServices();
}
