package org.palladiosimulator.retriever.mocore.processor.relation;

import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.Interface;
import org.palladiosimulator.retriever.mocore.surrogate.element.Signature;
import org.palladiosimulator.retriever.mocore.surrogate.relation.SignatureProvisionRelation;
import org.palladiosimulator.retriever.mocore.utility.ElementFactory;

import tools.mdsd.mocore.framework.processor.RelationProcessorTest;

public class SignatureProvisionRelationProcessorTest extends
        RelationProcessorTest<SignatureProvisionRelationProcessor, PcmSurrogate, SignatureProvisionRelation, Signature, Interface> {
    @Override
    protected SignatureProvisionRelation createRelation(final Signature source, final Interface destination,
            final boolean isPlaceholder) {
        return new SignatureProvisionRelation(source, destination, isPlaceholder);
    }

    @Override
    protected Signature getUniqueNonPlaceholderSourceEntity() {
        return ElementFactory.createUniqueSignature(false);
    }

    @Override
    protected Signature getPlaceholderOfSourceEntity(final Signature source) {
        return new Signature(source.getValue(), true);
    }

    @Override
    protected Interface getUniqueNonPlaceholderDestinationEntity() {
        return ElementFactory.createUniqueInterface(false);
    }

    @Override
    protected Interface getPlaceholderOfDestinationEntity(final Interface destination) {
        return new Interface(destination.getValue(), true);
    }

    @Override
    protected SignatureProvisionRelationProcessor createProcessor(final PcmSurrogate model) {
        return new SignatureProvisionRelationProcessor(model);
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }
}
