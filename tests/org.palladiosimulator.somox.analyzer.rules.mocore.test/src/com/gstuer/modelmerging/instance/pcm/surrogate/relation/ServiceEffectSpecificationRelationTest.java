package com.gstuer.modelmerging.instance.pcm.surrogate.relation;

import com.gstuer.modelmerging.framework.surrogate.RelationTest;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Component;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Interface;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.ServiceEffectSpecification;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Signature;

public class ServiceEffectSpecificationRelationTest extends RelationTest<ServiceEffectSpecificationRelation,
        ComponentSignatureProvisionRelation, ServiceEffectSpecification> {
    @Override
    protected ServiceEffectSpecificationRelation createRelation(ComponentSignatureProvisionRelation source,
            ServiceEffectSpecification destination, boolean isPlaceholder) {
        return new ServiceEffectSpecificationRelation(source, destination, isPlaceholder);
    }

    @Override
    protected ComponentSignatureProvisionRelation getUniqueSourceEntity() {
        Component component = Component.getUniquePlaceholder();
        Interface interfsc = Interface.getUniquePlaceholder();
        Signature signature = Signature.getUniquePlaceholder();
        InterfaceProvisionRelation interfaceProvision = new InterfaceProvisionRelation(component,
                interfsc, true);
        SignatureProvisionRelation signatureProvision = new SignatureProvisionRelation(signature,
                interfsc, true);
        return new ComponentSignatureProvisionRelation(interfaceProvision, signatureProvision, false);
    }

    @Override
    protected ServiceEffectSpecification getUniqueDestinationEntity() {
        return ServiceEffectSpecification.getUniquePlaceholder();
    }

}
