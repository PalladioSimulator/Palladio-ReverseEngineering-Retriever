package org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element;

import org.palladiosimulator.generator.fluent.repository.factory.FluentRepositoryFactory;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;

public class ServiceEffectSpecification extends PcmElement<ResourceDemandingSEFF> {
    public ServiceEffectSpecification(ResourceDemandingSEFF value, boolean isPlaceholder) {
        super(value, isPlaceholder);
    }

    public static ServiceEffectSpecification getUniquePlaceholder() {
        ResourceDemandingSEFF value = new FluentRepositoryFactory().newSeff()
                .withSeffBehaviour().withStartAction().followedBy().stopAction().createBehaviourNow().buildRDSeff();
        return new ServiceEffectSpecification(value, true);
    }
}
