package com.gstuer.modelmerging.instance.pcm.processor.relation;

import com.gstuer.modelmerging.framework.processor.RelationProcessorTest;
import com.gstuer.modelmerging.instance.pcm.surrogate.PcmSurrogate;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Component;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Deployment;
import com.gstuer.modelmerging.instance.pcm.surrogate.relation.ComponentAllocationRelation;
import com.gstuer.modelmerging.instance.pcm.utility.ElementFactory;

public class ComponentAllocationRelationProcessorTest
        extends RelationProcessorTest<ComponentAllocationRelationProcessor,
                PcmSurrogate, ComponentAllocationRelation, Component, Deployment> {
    @Override
    protected ComponentAllocationRelation createRelation(Component source, Deployment destination,
            boolean isPlaceholder) {
        return new ComponentAllocationRelation(source, destination, isPlaceholder);
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
    protected Deployment getUniqueNonPlaceholderDestinationEntity() {
        return ElementFactory.createUniqueDeployment(false);
    }

    @Override
    protected Deployment getPlaceholderOfDestinationEntity(Deployment destination) {
        return new Deployment(destination.getValue(), true);
    }

    @Override
    protected ComponentAllocationRelationProcessor createProcessor(PcmSurrogate model) {
        return new ComponentAllocationRelationProcessor(model);
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }
}
