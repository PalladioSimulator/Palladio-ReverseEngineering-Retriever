package org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.ServiceEffectSpecification;

import com.gstuer.modelmerging.framework.surrogate.Relation;
import com.gstuer.modelmerging.framework.surrogate.Replaceable;

public class ServiceEffectSpecificationRelation
        extends Relation<ComponentSignatureProvisionRelation, ServiceEffectSpecification> {
    public ServiceEffectSpecificationRelation(ComponentSignatureProvisionRelation source,
            ServiceEffectSpecification destination,
            boolean isPlaceholder) {
        super(source, destination, isPlaceholder);
    }

    @Override
    public <U extends Replaceable> ServiceEffectSpecificationRelation replace(U original, U replacement) {
        if (!this.includes(original)) {
            // TODO Add message to exception
            throw new IllegalArgumentException();
        }
        if (this.equals(original)) {
            return (ServiceEffectSpecificationRelation) replacement;
        }
        ComponentSignatureProvisionRelation source = getSourceReplacement(original, replacement);
        ServiceEffectSpecification destination = getDestinationReplacement(original, replacement);
        return new ServiceEffectSpecificationRelation(source, destination, this.isPlaceholder());
    }
}
