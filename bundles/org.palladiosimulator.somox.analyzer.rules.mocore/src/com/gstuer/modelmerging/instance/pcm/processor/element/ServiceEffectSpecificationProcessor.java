package com.gstuer.modelmerging.instance.pcm.processor.element;

import com.gstuer.modelmerging.framework.processor.Processor;
import com.gstuer.modelmerging.instance.pcm.surrogate.PcmSurrogate;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.ServiceEffectSpecification;

public class ServiceEffectSpecificationProcessor extends Processor<PcmSurrogate, ServiceEffectSpecification> {
    public ServiceEffectSpecificationProcessor(PcmSurrogate model) {
        super(model, ServiceEffectSpecification.class);
    }

    @Override
    protected void refine(ServiceEffectSpecification discovery) {
        // TODO Evaluate whether refinement should be done for a single specification element
    }
}
