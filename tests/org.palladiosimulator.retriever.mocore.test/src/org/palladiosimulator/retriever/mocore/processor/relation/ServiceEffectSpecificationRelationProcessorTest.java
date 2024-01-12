package org.palladiosimulator.retriever.mocore.processor.relation;

import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.Component;
import org.palladiosimulator.retriever.mocore.surrogate.element.Interface;
import org.palladiosimulator.retriever.mocore.surrogate.element.ServiceEffectSpecification;
import org.palladiosimulator.retriever.mocore.surrogate.element.Signature;
import org.palladiosimulator.retriever.mocore.surrogate.relation.ComponentSignatureProvisionRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.InterfaceProvisionRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.ServiceEffectSpecificationRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.SignatureProvisionRelation;
import org.palladiosimulator.retriever.mocore.utility.ElementFactory;

import tools.mdsd.mocore.framework.processor.RelationProcessorTest;

public class ServiceEffectSpecificationRelationProcessorTest extends
        RelationProcessorTest<ServiceEffectSpecificationRelationProcessor, PcmSurrogate, ServiceEffectSpecificationRelation, ComponentSignatureProvisionRelation, ServiceEffectSpecification> {
    @Override
    protected ServiceEffectSpecificationRelation createRelation(final ComponentSignatureProvisionRelation source,
            final ServiceEffectSpecification destination, final boolean isPlaceholder) {
        return new ServiceEffectSpecificationRelation(source, destination, isPlaceholder);
    }

    @Override
    protected ComponentSignatureProvisionRelation getUniqueNonPlaceholderSourceEntity() {
        final Component<?> component = Component.getUniquePlaceholder();
        final Interface interfsc = Interface.getUniquePlaceholder();
        final Signature signature = Signature.getUniquePlaceholder();
        final InterfaceProvisionRelation interfaceProvision = new InterfaceProvisionRelation(component, interfsc, true);
        final SignatureProvisionRelation signatureProvision = new SignatureProvisionRelation(signature, interfsc, true);
        return new ComponentSignatureProvisionRelation(interfaceProvision, signatureProvision, false);

    }

    @Override
    protected ComponentSignatureProvisionRelation getPlaceholderOfSourceEntity(
            final ComponentSignatureProvisionRelation source) {
        return new ComponentSignatureProvisionRelation(source.getSource(), source.getDestination(), true);
    }

    @Override
    protected ServiceEffectSpecification getUniqueNonPlaceholderDestinationEntity() {
        return ElementFactory.createUniqueServiceEffectSpecification(false);
    }

    @Override
    protected ServiceEffectSpecification getPlaceholderOfDestinationEntity(
            final ServiceEffectSpecification destination) {
        return new ServiceEffectSpecification(destination.getValue(), true);
    }

    @Override
    protected ServiceEffectSpecificationRelationProcessor createProcessor(final PcmSurrogate model) {
        return new ServiceEffectSpecificationRelationProcessor(model);
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }
}
