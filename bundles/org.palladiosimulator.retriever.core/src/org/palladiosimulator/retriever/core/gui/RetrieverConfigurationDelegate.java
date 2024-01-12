package org.palladiosimulator.retriever.core.gui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.palladiosimulator.retriever.core.configuration.RetrieverConfigurationImpl;
import org.palladiosimulator.retriever.core.configuration.RetrieverWorkflowConfiguration;
import org.palladiosimulator.retriever.core.workflow.RetrieverJob;

import de.uka.ipd.sdq.workflow.Workflow;
import de.uka.ipd.sdq.workflow.jobs.IJob;
import de.uka.ipd.sdq.workflow.launchconfig.AbstractWorkflowBasedLaunchConfigurationDelegate;

public class RetrieverConfigurationDelegate
        extends AbstractWorkflowBasedLaunchConfigurationDelegate<RetrieverWorkflowConfiguration, Workflow> {

    @Override
    protected IJob createWorkflowJob(RetrieverWorkflowConfiguration config, ILaunch launch) throws CoreException {
        return new RetrieverJob(config.getRetrieverConfiguration());
    }

    @Override
    protected RetrieverWorkflowConfiguration deriveConfiguration(ILaunchConfiguration configuration, String mode)
            throws CoreException {

        final RetrieverWorkflowConfiguration analyzerConfiguration = new RetrieverWorkflowConfiguration();
        analyzerConfiguration.setRetrieverConfiguration(new RetrieverConfigurationImpl(configuration.getAttributes()));

        return analyzerConfiguration;
    }

}
