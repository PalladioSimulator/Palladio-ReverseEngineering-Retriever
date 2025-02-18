/** */
package org.palladiosimulator.retriever.core.gui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.palladiosimulator.retriever.core.configuration.RetrieverConfigurationImpl;
import org.palladiosimulator.retriever.core.configuration.RetrieverWorkflowConfiguration;
import org.palladiosimulator.retriever.core.workflow.RetrieverJob;

import de.uka.ipd.sdq.workflow.Workflow;
import de.uka.ipd.sdq.workflow.WorkflowExceptionHandler;
import de.uka.ipd.sdq.workflow.jobs.IJob;
import de.uka.ipd.sdq.workflow.launchconfig.core.AbstractWorkflowBasedLaunchConfigurationDelegate;
import de.uka.ipd.sdq.workflow.ui.UIBasedWorkflowExceptionHandler;

public class RetrieverConfigurationDelegate
        extends AbstractWorkflowBasedLaunchConfigurationDelegate<RetrieverWorkflowConfiguration, Workflow> {
    @Override
    protected WorkflowExceptionHandler createExceptionHandler(boolean interactive) {
        return new UIBasedWorkflowExceptionHandler(!interactive);
    }

    @Override
    protected IJob createWorkflowJob(final RetrieverWorkflowConfiguration config, final ILaunch launch)
            throws CoreException {
        return new RetrieverJob(config.getRetrieverConfiguration());
    }

    @Override
    protected RetrieverWorkflowConfiguration deriveConfiguration(final ILaunchConfiguration configuration,
            final String mode) throws CoreException {

        final RetrieverWorkflowConfiguration analyzerConfiguration = new RetrieverWorkflowConfiguration();
        analyzerConfiguration.setRetrieverConfiguration(new RetrieverConfigurationImpl(configuration.getAttributes()));

        return analyzerConfiguration;
    }

}
