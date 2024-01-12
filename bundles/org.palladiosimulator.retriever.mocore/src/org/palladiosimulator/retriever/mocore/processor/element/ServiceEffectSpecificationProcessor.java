package org.palladiosimulator.retriever.mocore.processor.element;

import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.ServiceEffectSpecification;

import tools.mdsd.mocore.framework.processor.Processor;

public class ServiceEffectSpecificationProcessor extends Processor<PcmSurrogate, ServiceEffectSpecification> {
    public ServiceEffectSpecificationProcessor(final PcmSurrogate model) {
        super(model, ServiceEffectSpecification.class);
    }

    @Override
    protected void refine(final ServiceEffectSpecification discovery) {
        // TODO Evaluate whether refinement should be done for a single specification element
    }
}
