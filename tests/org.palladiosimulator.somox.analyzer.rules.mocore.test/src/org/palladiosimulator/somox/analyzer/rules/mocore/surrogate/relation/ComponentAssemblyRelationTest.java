package org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Component;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Interface;

import com.gstuer.modelmerging.framework.surrogate.RelationTest;

public class ComponentAssemblyRelationTest
        extends RelationTest<ComponentAssemblyRelation, InterfaceProvisionRelation, InterfaceRequirementRelation> {
    private static final Interface RELATION_DESTINATION = Interface.getUniquePlaceholder();

    @Override
    protected ComponentAssemblyRelation createRelation(InterfaceProvisionRelation source,
            InterfaceRequirementRelation destination,
            boolean isPlaceholder) {
        return new ComponentAssemblyRelation(source, destination, isPlaceholder);
    }

    @Override
    protected InterfaceProvisionRelation getUniqueSourceEntity() {
        Component source = Component.getUniquePlaceholder();
        return new InterfaceProvisionRelation(source, RELATION_DESTINATION, true);
    }

    @Override
    protected InterfaceRequirementRelation getUniqueDestinationEntity() {
        Component source = Component.getUniquePlaceholder();
        return new InterfaceRequirementRelation(source, RELATION_DESTINATION, true);
    }
}
