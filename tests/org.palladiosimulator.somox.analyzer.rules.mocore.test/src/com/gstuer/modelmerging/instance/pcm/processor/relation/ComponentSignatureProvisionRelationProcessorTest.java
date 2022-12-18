package com.gstuer.modelmerging.instance.pcm.processor.relation;

import com.gstuer.modelmerging.framework.processor.RelationProcessorTest;
import com.gstuer.modelmerging.instance.pcm.surrogate.PcmSurrogate;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Component;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Interface;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Signature;
import com.gstuer.modelmerging.instance.pcm.surrogate.relation.ComponentSignatureProvisionRelation;
import com.gstuer.modelmerging.instance.pcm.surrogate.relation.InterfaceProvisionRelation;
import com.gstuer.modelmerging.instance.pcm.surrogate.relation.SignatureProvisionRelation;

public class ComponentSignatureProvisionRelationProcessorTest
        extends RelationProcessorTest<ComponentSignatureProvisionRelationProcessor, PcmSurrogate,
                ComponentSignatureProvisionRelation, InterfaceProvisionRelation, SignatureProvisionRelation> {
    private static final Interface RELATION_INTERFACE = Interface.getUniquePlaceholder();

    @Override
    protected ComponentSignatureProvisionRelation createRelation(InterfaceProvisionRelation source,
            SignatureProvisionRelation destination, boolean isPlaceholder) {
        return new ComponentSignatureProvisionRelation(source, destination, isPlaceholder);
    }

    @Override
    protected InterfaceProvisionRelation getUniqueNonPlaceholderSourceEntity() {
        Component source = Component.getUniquePlaceholder();
        return new InterfaceProvisionRelation(source, RELATION_INTERFACE, false);
    }

    @Override
    protected InterfaceProvisionRelation getPlaceholderOfSourceEntity(InterfaceProvisionRelation source) {
        return new InterfaceProvisionRelation(source.getSource(), source.getDestination(), true);
    }

    @Override
    protected SignatureProvisionRelation getUniqueNonPlaceholderDestinationEntity() {
        Signature signature = Signature.getUniquePlaceholder();
        return new SignatureProvisionRelation(signature, RELATION_INTERFACE, false);
    }

    @Override
    protected SignatureProvisionRelation getPlaceholderOfDestinationEntity(SignatureProvisionRelation destination) {
        return new SignatureProvisionRelation(destination.getSource(), destination.getDestination(), true);
    }

    @Override
    protected ComponentSignatureProvisionRelationProcessor createProcessor(PcmSurrogate model) {
        return new ComponentSignatureProvisionRelationProcessor(model);
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }
}
