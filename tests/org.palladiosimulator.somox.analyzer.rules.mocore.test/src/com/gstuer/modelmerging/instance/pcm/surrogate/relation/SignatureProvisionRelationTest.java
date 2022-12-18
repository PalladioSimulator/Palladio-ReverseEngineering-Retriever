package com.gstuer.modelmerging.instance.pcm.surrogate.relation;

import com.gstuer.modelmerging.framework.surrogate.RelationTest;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Interface;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Signature;

public class SignatureProvisionRelationTest extends RelationTest<SignatureProvisionRelation, Signature, Interface> {
    @Override
    protected SignatureProvisionRelation createRelation(Signature source, Interface destination,
            boolean isPlaceholder) {
        return new SignatureProvisionRelation(source, destination, isPlaceholder);
    }

    @Override
    protected Signature getUniqueSourceEntity() {
        return Signature.getUniquePlaceholder();
    }

    @Override
    protected Interface getUniqueDestinationEntity() {
        return Interface.getUniquePlaceholder();
    }
}
