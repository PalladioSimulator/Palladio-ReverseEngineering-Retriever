package com.gstuer.modelmerging.instance.pcm.surrogate.relation;

import com.gstuer.modelmerging.framework.surrogate.RelationTest;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Component;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Interface;

public class InterfaceRequirementRelationTest extends RelationTest<InterfaceRequirementRelation, Component, Interface> {
    @Override
    protected InterfaceRequirementRelation createRelation(Component source, Interface destination,
            boolean isPlaceholder) {
        return new InterfaceRequirementRelation(source, destination, isPlaceholder);
    }

    @Override
    protected Component getUniqueSourceEntity() {
        return Component.getUniquePlaceholder();
    }

    @Override
    protected Interface getUniqueDestinationEntity() {
        return Interface.getUniquePlaceholder();
    }
}
