package org.palladiosimulator.somox.analyzer.rules.configuration;

import org.somox.ui.runconfig.ModelAnalyzerConfiguration;

public class RuleEngineAnalyzerConfiguration extends ModelAnalyzerConfiguration<RuleEngineConfiguration> {

    @Override
    public void setDefaults() {
        moxConfiguration = new RuleEngineConfiguration();
    }
}
