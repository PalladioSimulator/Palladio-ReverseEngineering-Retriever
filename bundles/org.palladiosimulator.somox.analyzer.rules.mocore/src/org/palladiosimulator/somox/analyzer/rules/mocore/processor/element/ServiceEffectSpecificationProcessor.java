package org.palladiosimulator.somox.analyzer.rules.mocore.processor.element;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.ServiceEffectSpecification;

import com.gstuer.modelmerging.framework.processor.Processor;

public class ServiceEffectSpecificationProcessor extends Processor<PcmSurrogate, ServiceEffectSpecification> {
    public ServiceEffectSpecificationProcessor(PcmSurrogate model) {
        super(model, ServiceEffectSpecification.class);
    }

    @Override
    protected void refine(ServiceEffectSpecification discovery) {
        // TODO Evaluate whether refinement should be done for a single specification element
    }
}
