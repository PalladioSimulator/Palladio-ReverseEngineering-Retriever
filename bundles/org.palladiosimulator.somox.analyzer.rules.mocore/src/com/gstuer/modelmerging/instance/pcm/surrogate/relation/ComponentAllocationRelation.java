package com.gstuer.modelmerging.instance.pcm.surrogate.relation;

import com.gstuer.modelmerging.framework.surrogate.Relation;
import com.gstuer.modelmerging.framework.surrogate.Replaceable;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Component;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Deployment;

public class ComponentAllocationRelation extends Relation<Component, Deployment> {
    public ComponentAllocationRelation(Component source, Deployment destination, boolean isPlaceholder) {
        super(source, destination, isPlaceholder);
    }

    @Override
    public <U extends Replaceable> ComponentAllocationRelation replace(U original, U replacement) {
        if (!this.includes(original)) {
            // TODO Add message to exception
            throw new IllegalArgumentException();
        }
        if (this.equals(original)) {
            return (ComponentAllocationRelation) replacement;
        }
        Component source = getSourceReplacement(original, replacement);
        Deployment destination = getDestinationReplacement(original, replacement);
        return new ComponentAllocationRelation(source, destination, this.isPlaceholder());
    }
}
