package org.palladiosimulator.retriever.mocore.processor.element;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.Interface;
import org.palladiosimulator.retriever.mocore.surrogate.relation.InterfaceProvisionRelation;

import tools.mdsd.mocore.framework.processor.ProcessorTest;
import tools.mdsd.mocore.framework.surrogate.Replaceable;

public class InterfaceProcessorTest extends ProcessorTest<InterfaceProcessor, PcmSurrogate, Interface> {
    @Test
    @DisabledIf(TEST_API_ONLY_METHOD_NAME)
    public void testRefineWithValidElementAddsCorrectImplications() {
        // Test data
        final PcmSurrogate model = this.createEmptyModel();
        final InterfaceProcessor processor = this.createProcessor(model);
        final Interface element = this.createUniqueReplaceable();

        // Assertions: Pre-execution
        assertTrue(processor.getImplications()
            .isEmpty());

        // Execution
        processor.refine(element);
        final Set<Replaceable> implications = processor.getImplications();

        // Assertions: Post-execution
        assertEquals(1, implications.size());
        final Replaceable implication = implications.stream()
            .findFirst()
            .orElseThrow();
        assertEquals(InterfaceProvisionRelation.class, implication.getClass());
        final InterfaceProvisionRelation relation = (InterfaceProvisionRelation) implication;
        assertEquals(element, relation.getDestination());
        assertTrue(relation.isPlaceholder());
        assertTrue(relation.getSource()
            .isPlaceholder());
        assertEquals(element.getValue()
            .getEntityName() + " Provider", relation.getSource()
                .getValue()
                .getEntityName());
    }

    @Override
    protected InterfaceProcessor createProcessor(final PcmSurrogate model) {
        return new InterfaceProcessor(model);
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }

    @Override
    protected Interface createUniqueReplaceable() {
        return Interface.getUniquePlaceholder();
    }
}
