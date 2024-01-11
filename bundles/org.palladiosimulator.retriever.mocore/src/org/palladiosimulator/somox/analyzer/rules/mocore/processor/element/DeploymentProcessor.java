package org.palladiosimulator.somox.analyzer.rules.mocore.processor.element;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Deployment;

import tools.mdsd.mocore.framework.processor.Processor;

public class DeploymentProcessor extends Processor<PcmSurrogate, Deployment> {
    public DeploymentProcessor(PcmSurrogate model) {
        super(model, Deployment.class);
    }

    @Override
    protected void refine(Deployment discovery) {
        // No refinement needed when adding a deployment element
    }
}
