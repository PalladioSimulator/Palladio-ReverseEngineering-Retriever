package org.palladiosimulator.retriever.core.gui;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.palladiosimulator.retriever.extraction.engine.Service;
import org.palladiosimulator.retriever.extraction.engine.ServiceConfiguration;

import de.uka.ipd.sdq.workflow.launchconfig.LaunchConfigPlugin;

public class ServiceConfigurationManager<T extends Service> {
    private final ServiceConfiguration<T> serviceConfiguration;

    public ServiceConfigurationManager(final ServiceConfiguration<T> serviceConfiguration) {
        this.serviceConfiguration = serviceConfiguration;
    }

    public void initializeFrom(final ILaunchConfiguration configuration) {
        try {
            this.serviceConfiguration.applyAttributeMap(configuration.getAttributes());
        } catch (final CoreException e) {
            LaunchConfigPlugin.log(IStatus.ERROR, e.getMessage());
        }
    }

    public void performApply(final ILaunchConfigurationWorkingCopy configuration) {
        // Update the LaunchConfiguration
        this.writeServiceConfigAttributes(configuration);
    }

    /**
     * Called when a new launch configuration is created, to set the values to sensible defaults.
     *
     * @param configuration
     *            the new launch configuration
     */
    public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
        this.writeServiceConfigAttributes(configuration);
    }

    private void writeServiceConfigAttributes(final ILaunchConfigurationWorkingCopy configuration) {
        final Map<String, Object> attributes = this.serviceConfiguration.toMap();
        for (final Map.Entry<String, Object> attribute : attributes.entrySet()) {
            configuration.setAttribute(attribute.getKey(), attribute.getValue());
        }
    }

    public ServiceConfiguration<T> getServiceConfiguration() {
        return this.serviceConfiguration;
    }
}
