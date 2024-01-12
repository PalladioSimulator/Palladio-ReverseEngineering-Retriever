package org.palladiosimulator.retriever.mocore.surrogate.relation;

import static org.junit.Assert.assertThrows;

import org.junit.jupiter.api.Test;
import org.palladiosimulator.retriever.mocore.surrogate.element.AtomicComponent;
import org.palladiosimulator.retriever.mocore.surrogate.element.Component;
import org.palladiosimulator.retriever.mocore.surrogate.element.Composite;
import org.palladiosimulator.retriever.mocore.surrogate.element.Interface;

import tools.mdsd.mocore.framework.surrogate.RelationTest;

public class CompositeProvisionDelegationRelationTest extends
        RelationTest<CompositeProvisionDelegationRelation, InterfaceProvisionRelation, InterfaceProvisionRelation> {
    private static final Interface RELATION_INTERFACE = Interface.getUniquePlaceholder();

    @Test
    public void testConstructorWithEqualSourceAndDestinationRelation() {
        final InterfaceProvisionRelation entity = this.getUniqueSourceEntity();

        assertThrows(IllegalArgumentException.class,
                () -> new CompositeProvisionDelegationRelation(entity, entity, false));
    }

    @Test
    public void testConstructorWithoutSourceCompositeComponent() {
        final AtomicComponent sourceComponent = AtomicComponent.getUniquePlaceholder();
        final Composite destinationComponent = Composite.getUniquePlaceholder();
        final InterfaceProvisionRelation source = new InterfaceProvisionRelation(sourceComponent, RELATION_INTERFACE,
                true);
        final InterfaceProvisionRelation destination = new InterfaceProvisionRelation(destinationComponent,
                RELATION_INTERFACE, true);

        assertThrows(IllegalArgumentException.class,
                () -> new CompositeProvisionDelegationRelation(source, destination, false));
    }

    @Test
    public void testConstructorWithDifferentRequirementInterfaces() {
        final InterfaceProvisionRelation source = this.getUniqueSourceEntity();
        final Component<?> destinationComponent = AtomicComponent.getUniquePlaceholder();
        final Interface destinationInterface = Interface.getUniquePlaceholder();
        final InterfaceProvisionRelation destination = new InterfaceProvisionRelation(destinationComponent,
                destinationInterface, true);

        assertThrows(IllegalArgumentException.class,
                () -> new CompositeProvisionDelegationRelation(source, destination, false));
    }

    @Override
    protected CompositeProvisionDelegationRelation createRelation(final InterfaceProvisionRelation source,
            final InterfaceProvisionRelation destination, final boolean isPlaceholder) {
        return new CompositeProvisionDelegationRelation(source, destination, isPlaceholder);
    }

    @Override
    protected InterfaceProvisionRelation getUniqueSourceEntity() {
        final Composite source = Composite.getUniquePlaceholder();
        return new InterfaceProvisionRelation(source, RELATION_INTERFACE, true);
    }

    @Override
    protected InterfaceProvisionRelation getUniqueDestinationEntity() {
        final Component<?> source = AtomicComponent.getUniquePlaceholder();
        return new InterfaceProvisionRelation(source, RELATION_INTERFACE, true);
    }
}
