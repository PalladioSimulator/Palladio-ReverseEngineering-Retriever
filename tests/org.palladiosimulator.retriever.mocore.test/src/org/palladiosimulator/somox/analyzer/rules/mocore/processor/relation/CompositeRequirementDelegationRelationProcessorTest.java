package org.palladiosimulator.somox.analyzer.rules.mocore.processor.relation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Component;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Composite;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Interface;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.CompositeRequirementDelegationRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.CompositionRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.InterfaceRequirementRelation;

import tools.mdsd.mocore.framework.processor.RelationProcessorTest;
import tools.mdsd.mocore.framework.surrogate.Replaceable;

public class CompositeRequirementDelegationRelationProcessorTest
        extends RelationProcessorTest<CompositeRequirementDelegationRelationProcessor, PcmSurrogate,
                CompositeRequirementDelegationRelation, InterfaceRequirementRelation, InterfaceRequirementRelation> {
    private static final Interface RELATION_INTERFACE = Interface.getUniquePlaceholder();

    @Test
    @DisabledIf(TEST_API_ONLY_METHOD_NAME)
    public void testRefineWithValidRelationAddsCorrectImplications() {
        // Test data
        PcmSurrogate model = createEmptyModel();
        CompositeRequirementDelegationRelationProcessor processor = createProcessor(model);
        CompositeRequirementDelegationRelation relation = createUniqueReplaceable();

        // Assertions: Pre-execution
        assertTrue(processor.getImplications().isEmpty());

        // Execution
        processor.refine(relation);
        Set<Replaceable> implications = new HashSet<>(processor.getImplications());

        // Assertions: Post-execution
        assertTrue(implications.remove(relation.getSource()));
        assertTrue(implications.remove(relation.getDestination()));

        // Implicit CompositionRelation between source & destination component
        assertEquals(1, implications.size());
        Replaceable implication = implications.stream().findFirst().orElseThrow();
        assertEquals(CompositionRelation.class, implication.getClass());

        CompositionRelation implicitComposition = (CompositionRelation) implication;
        assertTrue(implicitComposition.isPlaceholder());
        assertTrue(implicitComposition.getSource().equals(relation.getSource().getSource()));
        assertTrue(implicitComposition.getDestination().equals(relation.getDestination().getSource()));
    }

    @Override
    protected CompositeRequirementDelegationRelation createRelation(InterfaceRequirementRelation source,
            InterfaceRequirementRelation destination, boolean isPlaceholder) {
        return new CompositeRequirementDelegationRelation(source, destination, isPlaceholder);
    }

    @Override
    protected InterfaceRequirementRelation getUniqueNonPlaceholderSourceEntity() {
        Composite source = Composite.getUniquePlaceholder();
        return new InterfaceRequirementRelation(source, RELATION_INTERFACE, false);
    }

    @Override
    protected InterfaceRequirementRelation getPlaceholderOfSourceEntity(InterfaceRequirementRelation source) {
        return new InterfaceRequirementRelation(source.getSource(), source.getDestination(), true);
    }

    @Override
    protected InterfaceRequirementRelation getUniqueNonPlaceholderDestinationEntity() {
        Component<?> source = Component.getUniquePlaceholder();
        return new InterfaceRequirementRelation(source, RELATION_INTERFACE, false);
    }

    @Override
    protected InterfaceRequirementRelation getPlaceholderOfDestinationEntity(InterfaceRequirementRelation destination) {
        return new InterfaceRequirementRelation(destination.getSource(), destination.getDestination(), true);
    }

    @Override
    protected CompositeRequirementDelegationRelationProcessor createProcessor(PcmSurrogate model) {
        return new CompositeRequirementDelegationRelationProcessor(model);
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }
}
