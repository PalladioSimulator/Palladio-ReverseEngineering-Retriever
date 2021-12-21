package org.palladiosimulator.somox.analyzer.rules.configuration;

import org.somox.ui.runconfig.ModelAnalyzerConfiguration;

/**
 * An adapter for RuleEngineConfiguration, not more than a formality for use as a type parameter
 * AbstractWorkflowBasedLaunchConfigurationDelegate
 *
 * @see de.uka.ipd.sdq.workflow.launchconfig.AbstractWorkflowBasedLaunchConfigurationDelegate
 */
public class RuleEngineAnalyzerConfiguration extends ModelAnalyzerConfiguration<RuleEngineConfiguration> {

    @Override
    public void setDefaults() {
        moxConfiguration = new RuleEngineConfiguration();
    }
}
