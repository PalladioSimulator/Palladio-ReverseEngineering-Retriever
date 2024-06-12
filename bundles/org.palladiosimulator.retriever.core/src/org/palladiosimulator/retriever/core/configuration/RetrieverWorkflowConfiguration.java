package org.palladiosimulator.retriever.core.configuration;

import org.palladiosimulator.retriever.services.RetrieverConfiguration;

import de.uka.ipd.sdq.workflow.launchconfig.AbstractWorkflowBasedRunConfiguration;

/**
 * An adapter for RetrieverConfiguration, not more than a formality for use as a type parameter in
 * AbstractWorkflowBasedLaunchConfigurationDelegate
 *
 * @see de.uka.ipd.sdq.workflow.launchconfig.AbstractWorkflowBasedLaunchConfigurationDelegate
 */
public class RetrieverWorkflowConfiguration extends AbstractWorkflowBasedRunConfiguration {

    private RetrieverConfiguration configuration;

    @Override
    public void setDefaults() {
        this.configuration = new RetrieverConfigurationImpl();
    }

    public RetrieverConfiguration getRetrieverConfiguration() {
        return this.configuration;
    }

    public void setRetrieverConfiguration(final RetrieverConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }
}
