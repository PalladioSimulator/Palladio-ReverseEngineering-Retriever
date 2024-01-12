package org.palladiosimulator.retriever.mocore.surrogate.element;

import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.retriever.mocore.utility.ElementFactory;

import tools.mdsd.mocore.framework.surrogate.ElementTest;

public class ServiceEffectSpecificationTest extends ElementTest<ServiceEffectSpecification, ResourceDemandingSEFF> {
    @Override
    protected ServiceEffectSpecification createElement(final ResourceDemandingSEFF value, final boolean isPlaceholder) {
        return new ServiceEffectSpecification(value, isPlaceholder);
    }

    @Override
    protected ResourceDemandingSEFF getUniqueValue() {
        return ElementFactory.createUniqueServiceEffectSpecification(false)
            .getValue();
    }

    @Override
    protected ServiceEffectSpecification getUniqueNonPlaceholder() {
        return new ServiceEffectSpecification(this.getUniqueValue(), false);
    }

    @Override
    protected ServiceEffectSpecification getPlaceholderOf(final ServiceEffectSpecification replaceable) {
        return new ServiceEffectSpecification(replaceable.getValue(), true);
    }
}
