package com.gstuer.modelmerging.instance.pcm.surrogate.relation;

import com.gstuer.modelmerging.framework.surrogate.Relation;
import com.gstuer.modelmerging.framework.surrogate.Replaceable;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Component;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Interface;

public class InterfaceRequirementRelation extends Relation<Component, Interface> {
    public InterfaceRequirementRelation(Component source, Interface destination, boolean isPlaceholder) {
        super(source, destination, isPlaceholder);
    }

    @Override
    public <U extends Replaceable> InterfaceRequirementRelation replace(U original, U replacement) {
        if (!this.includes(original)) {
            // TODO Add message to exception
            throw new IllegalArgumentException();
        }
        if (this.equals(original)) {
            return (InterfaceRequirementRelation) replacement;
        }
        Component source = getSourceReplacement(original, replacement);
        Interface destination = getDestinationReplacement(original, replacement);
        return new InterfaceRequirementRelation(source, destination, this.isPlaceholder());
    }
}
