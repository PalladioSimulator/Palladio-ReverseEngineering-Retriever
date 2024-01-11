package org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation;

import static org.junit.Assert.assertThrows;

import org.junit.jupiter.api.Test;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.AtomicComponent;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Component;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Composite;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Interface;

import tools.mdsd.mocore.framework.surrogate.RelationTest;

public class CompositeRequirementDelegationRelationTest extends RelationTest<CompositeRequirementDelegationRelation,
        InterfaceRequirementRelation, InterfaceRequirementRelation> {
    private static final Interface RELATION_INTERFACE = Interface.getUniquePlaceholder();

    @Test
    public void testConstructorWithEqualSourceAndDestinationRelation() {
        InterfaceRequirementRelation entity = getUniqueSourceEntity();

        assertThrows(IllegalArgumentException.class,
                () -> new CompositeRequirementDelegationRelation(entity, entity, false));
    }

    @Test
    public void testConstructorWithoutSourceCompositeComponent() {
        AtomicComponent sourceComponent = AtomicComponent.getUniquePlaceholder();
        Composite destinationComponent = Composite.getUniquePlaceholder();
        InterfaceRequirementRelation source = new InterfaceRequirementRelation(sourceComponent,
                RELATION_INTERFACE, true);
        InterfaceRequirementRelation destination = new InterfaceRequirementRelation(destinationComponent,
                RELATION_INTERFACE, true);

        assertThrows(IllegalArgumentException.class,
                () -> new CompositeRequirementDelegationRelation(source, destination, false));
    }

    @Test
    public void testConstructorWithDifferentRequirementInterfaces() {
        InterfaceRequirementRelation source = getUniqueSourceEntity();
        Component<?> destinationComponent = AtomicComponent.getUniquePlaceholder();
        Interface destinationInterface = Interface.getUniquePlaceholder();
        InterfaceRequirementRelation destination = new InterfaceRequirementRelation(destinationComponent,
                destinationInterface, true);

        assertThrows(IllegalArgumentException.class,
                () -> new CompositeRequirementDelegationRelation(source, destination, false));
    }

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
