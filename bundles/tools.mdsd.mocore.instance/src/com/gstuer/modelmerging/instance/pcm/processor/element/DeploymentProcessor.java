package com.gstuer.modelmerging.instance.pcm.processor.element;

import com.gstuer.modelmerging.framework.processor.Processor;
import com.gstuer.modelmerging.instance.pcm.surrogate.PcmSurrogate;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Deployment;

public class DeploymentProcessor extends Processor<PcmSurrogate, Deployment> {
    public DeploymentProcessor(PcmSurrogate model) {
        super(model, Deployment.class);
    }

    @Override
    protected void refine(Deployment discovery) {
        // No refinement needed when adding a deployment element
    }
}
