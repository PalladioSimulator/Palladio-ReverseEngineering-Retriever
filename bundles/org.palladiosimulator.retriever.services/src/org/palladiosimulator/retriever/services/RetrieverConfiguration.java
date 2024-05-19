package org.palladiosimulator.retriever.services;

import org.eclipse.emf.common.util.URI;

import de.uka.ipd.sdq.workflow.extension.ExtendableJobConfiguration;

public interface RetrieverConfiguration extends ExtendableJobConfiguration {

    URI getInputFolder();

    void setInputFolder(URI createFileURI);

    URI getOutputFolder();

    void setOutputFolder(URI createFileURI);

    URI getRulesFolder();

    void setRulesFolder(URI createFileURI);

    <T extends Service> ServiceConfiguration<T> getConfig(Class<T> serviceClass);

}
