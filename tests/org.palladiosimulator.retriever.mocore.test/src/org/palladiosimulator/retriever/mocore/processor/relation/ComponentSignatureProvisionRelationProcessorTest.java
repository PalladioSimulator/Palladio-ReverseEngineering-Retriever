package org.palladiosimulator.retriever.mocore.processor.relation;

import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.Component;
import org.palladiosimulator.retriever.mocore.surrogate.element.Interface;
import org.palladiosimulator.retriever.mocore.surrogate.element.Signature;
import org.palladiosimulator.retriever.mocore.surrogate.relation.ComponentSignatureProvisionRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.InterfaceProvisionRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.SignatureProvisionRelation;

import tools.mdsd.mocore.framework.processor.RelationProcessorTest;

public class ComponentSignatureProvisionRelationProcessorTest extends
        RelationProcessorTest<ComponentSignatureProvisionRelationProcessor, PcmSurrogate, ComponentSignatureProvisionRelation, InterfaceProvisionRelation, SignatureProvisionRelation> {
    private static final Interface RELATION_INTERFACE = Interface.getUniquePlaceholder();

    @Override
    protected ComponentSignatureProvisionRelation createRelation(final InterfaceProvisionRelation source,
            final SignatureProvisionRelation destination, final boolean isPlaceholder) {
        return new ComponentSignatureProvisionRelation(source, destination, isPlaceholder);
    }

    @Override
    protected InterfaceProvisionRelation getUniqueNonPlaceholderSourceEntity() {
        final Component<?> source = Component.getUniquePlaceholder();
        return new InterfaceProvisionRelation(source, RELATION_INTERFACE, false);
    }

    @Override
    protected InterfaceProvisionRelation getPlaceholderOfSourceEntity(final InterfaceProvisionRelation source) {
        return new InterfaceProvisionRelation(source.getSource(), source.getDestination(), true);
    }

    @Override
    protected SignatureProvisionRelation getUniqueNonPlaceholderDestinationEntity() {
        final Signature signature = Signature.getUniquePlaceholder();
        return new SignatureProvisionRelation(signature, RELATION_INTERFACE, false);
    }

    @Override
    protected SignatureProvisionRelation getPlaceholderOfDestinationEntity(
            final SignatureProvisionRelation destination) {
        return new SignatureProvisionRelation(destination.getSource(), destination.getDestination(), true);
    }

    @Override
    protected ComponentSignatureProvisionRelationProcessor createProcessor(final PcmSurrogate model) {
        return new ComponentSignatureProvisionRelationProcessor(model);
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }
}
