package org.palladiosimulator.somox.analyzer.rules.mocore.processor.relation;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Component;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Interface;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.ServiceEffectSpecification;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Signature;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.ComponentSignatureProvisionRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.InterfaceProvisionRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.ServiceEffectSpecificationRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.SignatureProvisionRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.utility.ElementFactory;

import tools.mdsd.mocore.framework.processor.RelationProcessorTest;

public class ServiceEffectSpecificationRelationProcessorTest extends
        RelationProcessorTest<ServiceEffectSpecificationRelationProcessor,
                PcmSurrogate, ServiceEffectSpecificationRelation, ComponentSignatureProvisionRelation,
                ServiceEffectSpecification> {
    @Override
    protected ServiceEffectSpecificationRelation createRelation(ComponentSignatureProvisionRelation source,
            ServiceEffectSpecification destination,
            boolean isPlaceholder) {
        return new ServiceEffectSpecificationRelation(source, destination, isPlaceholder);
    }

    @Override
    protected ComponentSignatureProvisionRelation getUniqueNonPlaceholderSourceEntity() {
        Component<?> component = Component.getUniquePlaceholder();
        Interface interfsc = Interface.getUniquePlaceholder();
        Signature signature = Signature.getUniquePlaceholder();
        InterfaceProvisionRelation interfaceProvision = new InterfaceProvisionRelation(component,
                interfsc, true);
        SignatureProvisionRelation signatureProvision = new SignatureProvisionRelation(signature,
                interfsc, true);
        return new ComponentSignatureProvisionRelation(interfaceProvision, signatureProvision, false);

    }

    @Override
    protected ComponentSignatureProvisionRelation getPlaceholderOfSourceEntity(
            ComponentSignatureProvisionRelation source) {
        return new ComponentSignatureProvisionRelation(source.getSource(), source.getDestination(), true);
    }

    @Override
    protected ServiceEffectSpecification getUniqueNonPlaceholderDestinationEntity() {
        return ElementFactory.createUniqueServiceEffectSpecification(false);
    }

    @Override
    protected ServiceEffectSpecification getPlaceholderOfDestinationEntity(ServiceEffectSpecification destination) {
        return new ServiceEffectSpecification(destination.getValue(), true);
    }

    @Override
    protected ServiceEffectSpecificationRelationProcessor createProcessor(PcmSurrogate model) {
        return new ServiceEffectSpecificationRelationProcessor(model);
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }
}
