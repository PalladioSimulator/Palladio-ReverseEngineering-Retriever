package com.gstuer.modelmerging.instance.pcm.surrogate.element;

import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;

import com.gstuer.modelmerging.framework.surrogate.ElementTest;
import com.gstuer.modelmerging.instance.pcm.utility.ElementFactory;

public class ServiceEffectSpecificationTest
        extends ElementTest<ServiceEffectSpecification, ResourceDemandingSEFF> {
    @Override
    protected ServiceEffectSpecification createElement(ResourceDemandingSEFF value, boolean isPlaceholder) {
        return new ServiceEffectSpecification(value, isPlaceholder);
    }

    @Override
    protected ResourceDemandingSEFF getUniqueValue() {
        return ElementFactory.createUniqueServiceEffectSpecification(false).getValue();
    }

    @Override
    protected ServiceEffectSpecification getUniqueNonPlaceholder() {
        return new ServiceEffectSpecification(getUniqueValue(), false);
    }

    @Override
    protected ServiceEffectSpecification getPlaceholderOf(ServiceEffectSpecification replaceable) {
        return new ServiceEffectSpecification(replaceable.getValue(), true);
    }
}
