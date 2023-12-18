package org.palladiosimulator.somox.analyzer.rules.engine;

import org.eclipse.emf.common.util.URI;

import de.uka.ipd.sdq.workflow.extension.ExtendableJobConfiguration;

public interface RuleEngineConfiguration extends ExtendableJobConfiguration {

    URI getInputFolder();

    void setInputFolder(URI createFileURI);

    URI getOutputFolder();

    void setOutputFolder(URI createFileURI);

    <T extends Service> ServiceConfiguration<T> getConfig(Class<T> serviceClass);

}
