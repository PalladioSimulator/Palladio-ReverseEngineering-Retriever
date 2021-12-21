package org.palladiosimulator.somox.analyzer.rules.workflow;

import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineConfiguration;

import de.uka.ipd.sdq.workflow.jobs.AbstractJob;

/**
 * The defining interface of the org.palladiosimulator.somox.analyzer.rules.analyst extension point.
 * Implement this interface to extend the rule engine by an additional analyst that can then process
 * the generated model.
 * 
 * @author Florian Bossert
 */
public interface Analyst {
    AbstractJob create(RuleEngineConfiguration configuration, RuleEngineBlackboard blackboard);
}
