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

    public ServiceConfigurationManager(ServiceConfiguration<T> serviceConfiguration) {
        this.serviceConfiguration = serviceConfiguration;
    }

    public void initializeFrom(ILaunchConfiguration configuration) {
        try {
            serviceConfiguration.applyAttributeMap(configuration.getAttributes());
        } catch (CoreException e) {
            LaunchConfigPlugin.log(IStatus.ERROR, e.getMessage());
        }
    }

    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        // Update the LaunchConfiguration
        writeServiceConfigAttributes(configuration);
    }

    /**
     * Called when a new launch configuration is created, to set the values to sensible defaults.
     * 
     * @param configuration
     *            the new launch configuration
     */
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        writeServiceConfigAttributes(configuration);
    }

    private void writeServiceConfigAttributes(ILaunchConfigurationWorkingCopy configuration) {
        Map<String, Object> attributes = serviceConfiguration.toMap();
        for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
            configuration.setAttribute(attribute.getKey(), attribute.getValue());
        }
    }

    public ServiceConfiguration<T> getServiceConfiguration() {
        return serviceConfiguration;
    }
}
