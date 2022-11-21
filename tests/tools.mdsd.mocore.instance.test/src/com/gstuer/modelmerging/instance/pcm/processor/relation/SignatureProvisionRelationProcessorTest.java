package com.gstuer.modelmerging.instance.pcm.processor.relation;

import com.gstuer.modelmerging.framework.processor.RelationProcessorTest;
import com.gstuer.modelmerging.instance.pcm.surrogate.PcmSurrogate;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Interface;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Signature;
import com.gstuer.modelmerging.instance.pcm.surrogate.relation.SignatureProvisionRelation;
import com.gstuer.modelmerging.instance.pcm.utility.ElementFactory;

public class SignatureProvisionRelationProcessorTest extends RelationProcessorTest<SignatureProvisionRelationProcessor,
        PcmSurrogate, SignatureProvisionRelation, Signature, Interface> {
    @Override
    protected SignatureProvisionRelation createRelation(Signature source, Interface destination,
            boolean isPlaceholder) {
        return new SignatureProvisionRelation(source, destination, isPlaceholder);
    }

    @Override
    protected Signature getUniqueNonPlaceholderSourceEntity() {
        return ElementFactory.createUniqueSignature(false);
    }

    @Override
    protected Signature getPlaceholderOfSourceEntity(Signature source) {
        return new Signature(source.getValue(), true);
    }

    @Override
    protected Interface getUniqueNonPlaceholderDestinationEntity() {
        return ElementFactory.createUniqueInterface(false);
    }

    @Override
    protected Interface getPlaceholderOfDestinationEntity(Interface destination) {
        return new Interface(destination.getValue(), true);
    }

    @Override
    protected SignatureProvisionRelationProcessor createProcessor(PcmSurrogate model) {
        return new SignatureProvisionRelationProcessor(model);
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }
}
