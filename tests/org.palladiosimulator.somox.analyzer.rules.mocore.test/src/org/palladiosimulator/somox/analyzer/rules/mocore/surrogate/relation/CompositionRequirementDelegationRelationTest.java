package org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.AtomicComponent;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Component;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Composite;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Interface;

import tools.mdsd.mocore.framework.surrogate.RelationTest;

public class CompositionRequirementDelegationRelationTest extends RelationTest<CompositeRequirementDelegationRelation,
        InterfaceRequirementRelation, InterfaceRequirementRelation> {
    private static final Interface RELATION_INTERFACE = Interface.getUniquePlaceholder();

    @Override
    protected CompositeRequirementDelegationRelation createRelation(InterfaceRequirementRelation source,
            InterfaceRequirementRelation destination,
            boolean isPlaceholder) {
        return new CompositeRequirementDelegationRelation(source, destination, isPlaceholder);
    }

    @Override
    protected InterfaceRequirementRelation getUniqueSourceEntity() {
        Composite source = Composite.getUniquePlaceholder();
        return new InterfaceRequirementRelation(source, RELATION_INTERFACE, true);
    }

    @Override
    protected InterfaceRequirementRelation getUniqueDestinationEntity() {
        Component<?> source = AtomicComponent.getUniquePlaceholder();
        return new InterfaceRequirementRelation(source, RELATION_INTERFACE, true);
    }
}
