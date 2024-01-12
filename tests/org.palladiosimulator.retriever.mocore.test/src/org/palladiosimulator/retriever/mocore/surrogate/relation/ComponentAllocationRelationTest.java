package org.palladiosimulator.retriever.mocore.surrogate.relation;

import org.palladiosimulator.retriever.mocore.surrogate.element.Component;
import org.palladiosimulator.retriever.mocore.surrogate.element.Deployment;

import tools.mdsd.mocore.framework.surrogate.RelationTest;

public class ComponentAllocationRelationTest
        extends RelationTest<ComponentAllocationRelation, Component<?>, Deployment> {
    @Override
    protected ComponentAllocationRelation createRelation(final Component<?> source, final Deployment destination,
            final boolean isPlaceholder) {
        return new ComponentAllocationRelation(source, destination, isPlaceholder);
    }

    @Override
    protected Component<?> getUniqueSourceEntity() {
        return Component.getUniquePlaceholder();
    }

    @Override
    protected Deployment getUniqueDestinationEntity() {
        return Deployment.getUniquePlaceholder();
    }
}
