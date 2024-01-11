package org.palladiosimulator.retriever.mocore.surrogate.relation;

import org.palladiosimulator.retriever.mocore.surrogate.element.Component;
import org.palladiosimulator.retriever.mocore.surrogate.element.Interface;
import org.palladiosimulator.retriever.mocore.surrogate.relation.InterfaceProvisionRelation;

import tools.mdsd.mocore.framework.surrogate.RelationTest;

public class InterfaceProvisionRelationTest extends RelationTest<InterfaceProvisionRelation, Component<?>, Interface> {
    @Override
    protected InterfaceProvisionRelation createRelation(Component<?> source, Interface destination,
            boolean isPlaceholder) {
        return new InterfaceProvisionRelation(source, destination, isPlaceholder);
    }

    @Override
    protected Component<?> getUniqueSourceEntity() {
        return Component.getUniquePlaceholder();
    }

    @Override
    protected Interface getUniqueDestinationEntity() {
        return Interface.getUniquePlaceholder();
    }
}
