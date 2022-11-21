package com.gstuer.modelmerging.instance.pcm.processor.relation;

import com.gstuer.modelmerging.framework.processor.RelationProcessorTest;
import com.gstuer.modelmerging.instance.pcm.surrogate.PcmSurrogate;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Component;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Interface;
import com.gstuer.modelmerging.instance.pcm.surrogate.relation.InterfaceRequirementRelation;
import com.gstuer.modelmerging.instance.pcm.utility.ElementFactory;

public class InterfaceRequirementRelationProcessorTest
        extends RelationProcessorTest<InterfaceRequirementRelationProcessor,
                PcmSurrogate, InterfaceRequirementRelation, Component, Interface> {
    @Override
    protected InterfaceRequirementRelation createRelation(Component source, Interface destination,
            boolean isPlaceholder) {
        return new InterfaceRequirementRelation(source, destination, isPlaceholder);
    }

    @Override
    protected Component getUniqueNonPlaceholderSourceEntity() {
        return ElementFactory.createUniqueComponent(false);
    }

    @Override
    protected Component getPlaceholderOfSourceEntity(Component source) {
        return new Component(source.getValue(), true);
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
    protected InterfaceRequirementRelationProcessor createProcessor(PcmSurrogate model) {
        return new InterfaceRequirementRelationProcessor(model);
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }
}
