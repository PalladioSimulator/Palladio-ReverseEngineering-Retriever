package org.palladiosimulator.retriever.mocore.processor.element;

import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.Deployment;

import tools.mdsd.mocore.framework.processor.Processor;

public class DeploymentProcessor extends Processor<PcmSurrogate, Deployment> {
    public DeploymentProcessor(final PcmSurrogate model) {
        super(model, Deployment.class);
    }

    @Override
    protected void refine(final Deployment discovery) {
        // No refinement needed when adding a deployment element
    }
}
