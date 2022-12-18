package org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Component;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Deployment;

import com.gstuer.modelmerging.framework.surrogate.RelationTest;

public class ComponentAllocationRelationTest extends RelationTest<ComponentAllocationRelation, Component, Deployment> {
    @Override
    protected ComponentAllocationRelation createRelation(Component source, Deployment destination,
            boolean isPlaceholder) {
        return new ComponentAllocationRelation(source, destination, isPlaceholder);
    }

    @Override
    protected Component getUniqueSourceEntity() {
        return Component.getUniquePlaceholder();
    }

    @Override
    protected Deployment getUniqueDestinationEntity() {
        return Deployment.getUniquePlaceholder();
    }
}
