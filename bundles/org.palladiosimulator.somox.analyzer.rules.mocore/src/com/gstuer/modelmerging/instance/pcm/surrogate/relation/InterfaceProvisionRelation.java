package com.gstuer.modelmerging.instance.pcm.surrogate.relation;

import com.gstuer.modelmerging.framework.surrogate.Relation;
import com.gstuer.modelmerging.framework.surrogate.Replaceable;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Component;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Interface;

public class InterfaceProvisionRelation extends Relation<Component, Interface> {
    public InterfaceProvisionRelation(Component source, Interface destination, boolean isPlaceholder) {
        super(source, destination, isPlaceholder);
    }

    @Override
    public <U extends Replaceable> InterfaceProvisionRelation replace(U original, U replacement) {
        if (!this.includes(original)) {
            // TODO Add message to exception
            throw new IllegalArgumentException();
        }
        if (this.equals(original)) {
            return (InterfaceProvisionRelation) replacement;
        }
        Component source = getSourceReplacement(original, replacement);
        Interface destination = getDestinationReplacement(original, replacement);
        return new InterfaceProvisionRelation(source, destination, this.isPlaceholder());
    }
}
