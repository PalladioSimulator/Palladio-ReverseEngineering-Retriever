package org.palladiosimulator.retriever.mocore.surrogate.relation;

import org.palladiosimulator.retriever.mocore.surrogate.element.Component;
import org.palladiosimulator.retriever.mocore.surrogate.element.Interface;

import tools.mdsd.mocore.framework.surrogate.RelationTest;

public class InterfaceRequirementRelationTest
        extends RelationTest<InterfaceRequirementRelation, Component<?>, Interface> {
    @Override
    protected InterfaceRequirementRelation createRelation(final Component<?> source, final Interface destination,
            final boolean isPlaceholder) {
        return new InterfaceRequirementRelation(source, destination, isPlaceholder);
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
