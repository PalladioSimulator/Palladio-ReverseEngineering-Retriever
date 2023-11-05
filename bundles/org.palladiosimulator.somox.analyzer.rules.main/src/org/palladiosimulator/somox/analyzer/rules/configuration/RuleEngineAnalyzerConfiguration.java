package org.palladiosimulator.somox.analyzer.rules.configuration;

import org.palladiosimulator.somox.analyzer.rules.engine.RuleEngineConfiguration;

import de.uka.ipd.sdq.workflow.launchconfig.AbstractWorkflowBasedRunConfiguration;

/**
 * An adapter for RuleEngineConfiguration, not more than a formality for use as a type parameter
 * AbstractWorkflowBasedLaunchConfigurationDelegate
 *
 * @see de.uka.ipd.sdq.workflow.launchconfig.AbstractWorkflowBasedLaunchConfigurationDelegate
 */
public class RuleEngineAnalyzerConfiguration extends AbstractWorkflowBasedRunConfiguration {

    private RuleEngineConfiguration ruleEngineConfiguration;

    @Override
    public void setDefaults() {
        ruleEngineConfiguration = new RuleEngineConfigurationImpl();
    }

    public RuleEngineConfiguration getRuleEngineConfiguration() {
        return ruleEngineConfiguration;
    }

    public void setRuleEngineConfiguration(RuleEngineConfiguration ruleEngineConfiguration) {
        this.ruleEngineConfiguration = ruleEngineConfiguration;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }
}
