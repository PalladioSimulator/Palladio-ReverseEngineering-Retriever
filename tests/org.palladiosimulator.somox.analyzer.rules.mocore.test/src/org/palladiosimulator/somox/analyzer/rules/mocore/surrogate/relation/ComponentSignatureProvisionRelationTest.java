package org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Component;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Interface;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Signature;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.ComponentSignatureProvisionRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.InterfaceProvisionRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.SignatureProvisionRelation;

import com.gstuer.modelmerging.framework.surrogate.RelationTest;

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
