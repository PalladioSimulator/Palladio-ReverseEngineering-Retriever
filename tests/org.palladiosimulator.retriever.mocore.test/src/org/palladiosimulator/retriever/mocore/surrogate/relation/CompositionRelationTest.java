package org.palladiosimulator.retriever.mocore.surrogate.relation;

import org.palladiosimulator.retriever.mocore.surrogate.element.Component;
import org.palladiosimulator.retriever.mocore.surrogate.element.Composite;

import tools.mdsd.mocore.framework.surrogate.RelationTest;

public class CompositionRelationTest extends RelationTest<CompositionRelation, Composite, Component<?>> {
    @Override
    protected CompositionRelation createRelation(final Composite source, final Component<?> destination,
            final boolean isPlaceholder) {
        return new CompositionRelation(source, destination, isPlaceholder);
    }

    @Override
    protected Composite getUniqueSourceEntity() {
        return Composite.getUniquePlaceholder();
    }

    @Override
    protected Component<?> getUniqueDestinationEntity() {
        return Component.getUniquePlaceholder();
    }
}
