package org.palladiosimulator.retriever.mocore.surrogate.relation;

import org.palladiosimulator.retriever.mocore.surrogate.element.Component;
import org.palladiosimulator.retriever.mocore.surrogate.element.Interface;
import org.palladiosimulator.retriever.mocore.surrogate.element.ServiceEffectSpecification;
import org.palladiosimulator.retriever.mocore.surrogate.element.Signature;

import tools.mdsd.mocore.framework.surrogate.RelationTest;

public class ServiceEffectSpecificationRelationTest extends
        RelationTest<ServiceEffectSpecificationRelation, ComponentSignatureProvisionRelation, ServiceEffectSpecification> {
    @Override
    protected ServiceEffectSpecificationRelation createRelation(final ComponentSignatureProvisionRelation source,
            final ServiceEffectSpecification destination, final boolean isPlaceholder) {
        return new ServiceEffectSpecificationRelation(source, destination, isPlaceholder);
    }

    @Override
    protected ComponentSignatureProvisionRelation getUniqueSourceEntity() {
        final Component<?> component = Component.getUniquePlaceholder();
        final Interface interfsc = Interface.getUniquePlaceholder();
        final Signature signature = Signature.getUniquePlaceholder();
        final InterfaceProvisionRelation interfaceProvision = new InterfaceProvisionRelation(component, interfsc, true);
        final SignatureProvisionRelation signatureProvision = new SignatureProvisionRelation(signature, interfsc, true);
        return new ComponentSignatureProvisionRelation(interfaceProvision, signatureProvision, false);
    }

    @Override
    protected ServiceEffectSpecification getUniqueDestinationEntity() {
        return ServiceEffectSpecification.getUniquePlaceholder();
    }

}
