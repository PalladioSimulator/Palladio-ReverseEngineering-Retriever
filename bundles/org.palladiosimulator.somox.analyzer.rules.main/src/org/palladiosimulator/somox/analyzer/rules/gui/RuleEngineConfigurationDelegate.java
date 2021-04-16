package org.palladiosimulator.somox.analyzer.rules.gui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineAnalyzerConfiguration;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineConfiguration;
import org.palladiosimulator.somox.analyzer.rules.workflow.RuleEngineJob;

import de.uka.ipd.sdq.workflow.Workflow;
import de.uka.ipd.sdq.workflow.jobs.IJob;
import de.uka.ipd.sdq.workflow.launchconfig.AbstractWorkflowBasedLaunchConfigurationDelegate;

public class RuleEngineConfigurationDelegate
        extends AbstractWorkflowBasedLaunchConfigurationDelegate<RuleEngineAnalyzerConfiguration, Workflow> {

    @Override
    protected IJob createWorkflowJob(RuleEngineAnalyzerConfiguration config, ILaunch launch) throws CoreException {
        return new RuleEngineJob(config);
    }

    @Override
    protected RuleEngineAnalyzerConfiguration deriveConfiguration(ILaunchConfiguration configuration, String mode)
            throws CoreException {

        final RuleEngineAnalyzerConfiguration analyzerConfiguration = new RuleEngineAnalyzerConfiguration();
        analyzerConfiguration.setMoxConfiguration(new RuleEngineConfiguration(configuration.getAttributes()));

        return analyzerConfiguration;
    }

}
