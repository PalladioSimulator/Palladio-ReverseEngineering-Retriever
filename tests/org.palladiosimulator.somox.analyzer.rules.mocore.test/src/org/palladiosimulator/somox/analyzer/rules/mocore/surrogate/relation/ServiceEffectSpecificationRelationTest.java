package org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Component;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Interface;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.ServiceEffectSpecification;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Signature;

import com.gstuer.modelmerging.framework.surrogate.RelationTest;

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
