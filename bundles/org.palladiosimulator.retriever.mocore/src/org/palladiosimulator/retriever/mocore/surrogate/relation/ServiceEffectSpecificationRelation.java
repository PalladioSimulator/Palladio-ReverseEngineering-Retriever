package org.palladiosimulator.retriever.mocore.surrogate.relation;

import org.palladiosimulator.retriever.mocore.surrogate.element.ServiceEffectSpecification;

import tools.mdsd.mocore.framework.surrogate.Relation;
import tools.mdsd.mocore.framework.surrogate.Replaceable;

public class ServiceEffectSpecificationRelation
        extends Relation<ComponentSignatureProvisionRelation, ServiceEffectSpecification> {
    public ServiceEffectSpecificationRelation(final ComponentSignatureProvisionRelation source,
            final ServiceEffectSpecification destination, final boolean isPlaceholder) {
        super(source, destination, isPlaceholder);
    }

    @Override
    public <U extends Replaceable> ServiceEffectSpecificationRelation replace(final U original, final U replacement) {
        if (!this.includes(original)) {
            // TODO Add message to exception
            throw new IllegalArgumentException();
        }
        if (this.equals(original)) {
            return (ServiceEffectSpecificationRelation) replacement;
        }
        final ComponentSignatureProvisionRelation source = this.getSourceReplacement(original, replacement);
        final ServiceEffectSpecification destination = this.getDestinationReplacement(original, replacement);
        return new ServiceEffectSpecificationRelation(source, destination, this.isPlaceholder());
    }
}
