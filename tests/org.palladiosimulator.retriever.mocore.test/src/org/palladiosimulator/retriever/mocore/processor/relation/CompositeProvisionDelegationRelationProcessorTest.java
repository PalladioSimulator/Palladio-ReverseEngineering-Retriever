package org.palladiosimulator.retriever.mocore.processor.relation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.palladiosimulator.retriever.mocore.processor.relation.CompositeProvisionDelegationRelationProcessor;
import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.Component;
import org.palladiosimulator.retriever.mocore.surrogate.element.Composite;
import org.palladiosimulator.retriever.mocore.surrogate.element.Interface;
import org.palladiosimulator.retriever.mocore.surrogate.relation.CompositeProvisionDelegationRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.CompositionRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.InterfaceProvisionRelation;

import tools.mdsd.mocore.framework.processor.RelationProcessorTest;
import tools.mdsd.mocore.framework.surrogate.Replaceable;

public class CompositeProvisionDelegationRelationProcessorTest extends
        RelationProcessorTest<CompositeProvisionDelegationRelationProcessor, PcmSurrogate, CompositeProvisionDelegationRelation, InterfaceProvisionRelation, InterfaceProvisionRelation> {
    private static final Interface RELATION_INTERFACE = Interface.getUniquePlaceholder();

    @Test
    @DisabledIf(TEST_API_ONLY_METHOD_NAME)
    public void testRefineWithValidRelationAddsCorrectImplications() {
        // Test data
        PcmSurrogate model = createEmptyModel();
        CompositeProvisionDelegationRelationProcessor processor = createProcessor(model);
        CompositeProvisionDelegationRelation relation = createUniqueReplaceable();

        // Assertions: Pre-execution
        assertTrue(processor.getImplications()
            .isEmpty());

        // Execution
        processor.refine(relation);
        Set<Replaceable> implications = new HashSet<>(processor.getImplications());

        // Assertions: Post-execution
        assertTrue(implications.remove(relation.getSource()));
        assertTrue(implications.remove(relation.getDestination()));

        // Implicit CompositionRelation between source & destination component
        assertEquals(1, implications.size());
        Replaceable implication = implications.stream()
            .findFirst()
            .orElseThrow();
        assertEquals(CompositionRelation.class, implication.getClass());

        CompositionRelation implicitComposition = (CompositionRelation) implication;
        assertTrue(implicitComposition.isPlaceholder());
        assertTrue(implicitComposition.getSource()
            .equals(relation.getSource()
                .getSource()));
        assertTrue(implicitComposition.getDestination()
            .equals(relation.getDestination()
                .getSource()));
    }

    @Override
    protected CompositeProvisionDelegationRelation createRelation(InterfaceProvisionRelation source,
            InterfaceProvisionRelation destination, boolean isPlaceholder) {
        return new CompositeProvisionDelegationRelation(source, destination, isPlaceholder);
    }

    @Override
    protected InterfaceProvisionRelation getUniqueNonPlaceholderSourceEntity() {
        Composite source = Composite.getUniquePlaceholder();
        return new InterfaceProvisionRelation(source, RELATION_INTERFACE, false);
    }

    @Override
    protected InterfaceProvisionRelation getPlaceholderOfSourceEntity(InterfaceProvisionRelation source) {
        return new InterfaceProvisionRelation(source.getSource(), source.getDestination(), true);
    }

    @Override
    protected InterfaceProvisionRelation getUniqueNonPlaceholderDestinationEntity() {
        Component<?> source = Component.getUniquePlaceholder();
        return new InterfaceProvisionRelation(source, RELATION_INTERFACE, false);
    }

    @Override
    protected InterfaceProvisionRelation getPlaceholderOfDestinationEntity(InterfaceProvisionRelation destination) {
        return new InterfaceProvisionRelation(destination.getSource(), destination.getDestination(), true);
    }

    @Override
    protected CompositeProvisionDelegationRelationProcessor createProcessor(PcmSurrogate model) {
        return new CompositeProvisionDelegationRelationProcessor(model);
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }
}
