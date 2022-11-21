package com.gstuer.modelmerging.instance.pcm.surrogate.relation;

import com.gstuer.modelmerging.framework.surrogate.RelationTest;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Component;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Interface;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Signature;

public class ComponentSignatureProvisionRelationTest extends
        RelationTest<ComponentSignatureProvisionRelation, InterfaceProvisionRelation, SignatureProvisionRelation> {
    private static final Interface RELATION_INTERFACE = Interface.getUniquePlaceholder();

    @Override
    protected ComponentSignatureProvisionRelation createRelation(InterfaceProvisionRelation source,
            SignatureProvisionRelation destination, boolean isPlaceholder) {
        return new ComponentSignatureProvisionRelation(source, destination, isPlaceholder);
    }

    @Override
    protected InterfaceProvisionRelation getUniqueSourceEntity() {
        Component source = Component.getUniquePlaceholder();
        return new InterfaceProvisionRelation(source, RELATION_INTERFACE, true);
    }

    @Override
    protected SignatureProvisionRelation getUniqueDestinationEntity() {
        Signature signature = Signature.getUniquePlaceholder();
        return new SignatureProvisionRelation(signature, RELATION_INTERFACE, true);
    }

}
