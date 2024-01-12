package org.palladiosimulator.retriever.mocore.surrogate.relation;

import static org.junit.Assert.assertThrows;

import org.junit.jupiter.api.Test;
import org.palladiosimulator.retriever.mocore.surrogate.element.AtomicComponent;
import org.palladiosimulator.retriever.mocore.surrogate.element.Component;
import org.palladiosimulator.retriever.mocore.surrogate.element.Composite;
import org.palladiosimulator.retriever.mocore.surrogate.element.Interface;

import tools.mdsd.mocore.framework.surrogate.RelationTest;

public class CompositeRequirementDelegationRelationTest extends
        RelationTest<CompositeRequirementDelegationRelation, InterfaceRequirementRelation, InterfaceRequirementRelation> {
    private static final Interface RELATION_INTERFACE = Interface.getUniquePlaceholder();

    @Test
    public void testConstructorWithEqualSourceAndDestinationRelation() {
        final InterfaceRequirementRelation entity = this.getUniqueSourceEntity();

        assertThrows(IllegalArgumentException.class,
                () -> new CompositeRequirementDelegationRelation(entity, entity, false));
    }

    @Test
    public void testConstructorWithoutSourceCompositeComponent() {
        final AtomicComponent sourceComponent = AtomicComponent.getUniquePlaceholder();
        final Composite destinationComponent = Composite.getUniquePlaceholder();
        final InterfaceRequirementRelation source = new InterfaceRequirementRelation(sourceComponent,
                RELATION_INTERFACE, true);
        final InterfaceRequirementRelation destination = new InterfaceRequirementRelation(destinationComponent,
                RELATION_INTERFACE, true);

        assertThrows(IllegalArgumentException.class,
                () -> new CompositeRequirementDelegationRelation(source, destination, false));
    }

    @Test
    public void testConstructorWithDifferentRequirementInterfaces() {
        final InterfaceRequirementRelation source = this.getUniqueSourceEntity();
        final Component<?> destinationComponent = AtomicComponent.getUniquePlaceholder();
        final Interface destinationInterface = Interface.getUniquePlaceholder();
        final InterfaceRequirementRelation destination = new InterfaceRequirementRelation(destinationComponent,
                destinationInterface, true);

        assertThrows(IllegalArgumentException.class,
                () -> new CompositeRequirementDelegationRelation(source, destination, false));
    }

    @Override
    protected CompositeRequirementDelegationRelation createRelation(final InterfaceRequirementRelation source,
            final InterfaceRequirementRelation destination, final boolean isPlaceholder) {
        return new CompositeRequirementDelegationRelation(source, destination, isPlaceholder);
    }

    @Override
    protected InterfaceRequirementRelation getUniqueSourceEntity() {
        final Composite source = Composite.getUniquePlaceholder();
        return new InterfaceRequirementRelation(source, RELATION_INTERFACE, true);
    }

    @Override
    protected InterfaceRequirementRelation getUniqueDestinationEntity() {
        final Component<?> source = AtomicComponent.getUniquePlaceholder();
        return new InterfaceRequirementRelation(source, RELATION_INTERFACE, true);
    }
}
