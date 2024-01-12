package org.palladiosimulator.retriever.mocore.surrogate.element;

import org.palladiosimulator.generator.fluent.repository.factory.FluentRepositoryFactory;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;

public class ServiceEffectSpecification extends PcmElement<ResourceDemandingSEFF> {
    public ServiceEffectSpecification(final ResourceDemandingSEFF value, final boolean isPlaceholder) {
        super(value, isPlaceholder);
    }

    public static ServiceEffectSpecification getUniquePlaceholder() {
        final ResourceDemandingSEFF value = new FluentRepositoryFactory().newSeff()
            .withSeffBehaviour()
            .withStartAction()
            .followedBy()
            .stopAction()
            .createBehaviourNow()
            .buildRDSeff();
        return new ServiceEffectSpecification(value, true);
    }
}
