package com.gstuer.modelmerging.instance.pcm.surrogate.relation;

import com.gstuer.modelmerging.framework.surrogate.Relation;
import com.gstuer.modelmerging.framework.surrogate.Replaceable;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Interface;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Signature;

public class SignatureProvisionRelation extends Relation<Signature, Interface> {
    public SignatureProvisionRelation(Signature source, Interface destination, boolean isPlaceholder) {
        super(source, destination, isPlaceholder);
    }

    @Override
    public <U extends Replaceable> SignatureProvisionRelation replace(U original, U replacement) {
        if (!this.includes(original)) {
            // TODO Add message to exception
            throw new IllegalArgumentException();
        }
        if (this.equals(original)) {
            return (SignatureProvisionRelation) replacement;
        }
        Signature source = getSourceReplacement(original, replacement);
        Interface destination = getDestinationReplacement(original, replacement);
        return new SignatureProvisionRelation(source, destination, this.isPlaceholder());
    }
}
