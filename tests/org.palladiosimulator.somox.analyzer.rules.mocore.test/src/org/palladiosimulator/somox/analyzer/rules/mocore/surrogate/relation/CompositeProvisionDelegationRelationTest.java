package org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation;

import static org.junit.Assert.assertThrows;

import org.junit.jupiter.api.Test;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.AtomicComponent;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Component;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Composite;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Interface;

import tools.mdsd.mocore.framework.surrogate.RelationTest;

public class CompositeProvisionDelegationRelationTest extends RelationTest<CompositeProvisionDelegationRelation,
        InterfaceProvisionRelation, InterfaceProvisionRelation> {
    private static final Interface RELATION_INTERFACE = Interface.getUniquePlaceholder();

    @Test
    public void testConstructorWithEqualSourceAndDestinationRelation() {
        InterfaceProvisionRelation entity = getUniqueSourceEntity();

        assertThrows(IllegalArgumentException.class,
                () -> new CompositeProvisionDelegationRelation(entity, entity, false));
    }

    @Test
    public void testConstructorWithoutSourceCompositeComponent() {
        AtomicComponent sourceComponent = AtomicComponent.getUniquePlaceholder();
        Composite destinationComponent = Composite.getUniquePlaceholder();
        InterfaceProvisionRelation source = new InterfaceProvisionRelation(sourceComponent,
                RELATION_INTERFACE, true);
        InterfaceProvisionRelation destination = new InterfaceProvisionRelation(destinationComponent,
                RELATION_INTERFACE, true);

        assertThrows(IllegalArgumentException.class,
                () -> new CompositeProvisionDelegationRelation(source, destination, false));
    }

    @Test
    public void testConstructorWithDifferentRequirementInterfaces() {
        InterfaceProvisionRelation source = getUniqueSourceEntity();
        Component<?> destinationComponent = AtomicComponent.getUniquePlaceholder();
        Interface destinationInterface = Interface.getUniquePlaceholder();
        InterfaceProvisionRelation destination = new InterfaceProvisionRelation(destinationComponent,
                destinationInterface, true);

        assertThrows(IllegalArgumentException.class,
                () -> new CompositeProvisionDelegationRelation(source, destination, false));
    }

    @Override
    protected CompositeProvisionDelegationRelation createRelation(InterfaceProvisionRelation source,
            InterfaceProvisionRelation destination,
            boolean isPlaceholder) {
        return new CompositeProvisionDelegationRelation(source, destination, isPlaceholder);
    }

    @Override
    protected InterfaceProvisionRelation getUniqueSourceEntity() {
        Composite source = Composite.getUniquePlaceholder();
        return new InterfaceProvisionRelation(source, RELATION_INTERFACE, true);
    }

    @Override
    protected InterfaceProvisionRelation getUniqueDestinationEntity() {
        Component<?> source = AtomicComponent.getUniquePlaceholder();
        return new InterfaceProvisionRelation(source, RELATION_INTERFACE, true);
    }
}
