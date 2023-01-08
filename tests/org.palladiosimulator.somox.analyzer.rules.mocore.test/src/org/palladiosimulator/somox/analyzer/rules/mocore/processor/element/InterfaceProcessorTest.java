package org.palladiosimulator.somox.analyzer.rules.mocore.processor.element;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Interface;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.InterfaceProvisionRelation;

import tools.mdsd.mocore.framework.processor.ProcessorTest;
import tools.mdsd.mocore.framework.surrogate.Replaceable;

public class InterfaceProcessorTest extends ProcessorTest<InterfaceProcessor, PcmSurrogate, Interface> {
    @Test
    @DisabledIf(TEST_API_ONLY_METHOD_NAME)
    public void testRefineWithValidElementAddsCorrectImplications() {
        // Test data
        PcmSurrogate model = createEmptyModel();
        InterfaceProcessor processor = createProcessor(model);
        Interface element = createUniqueReplaceable();

        // Assertions: Pre-execution
        assertTrue(processor.getImplications().isEmpty());

        // Execution
        processor.refine(element);
        Set<Replaceable> implications = processor.getImplications();

        // Assertions: Post-execution
        assertEquals(1, implications.size());
        Replaceable implication = implications.stream().findFirst().orElseThrow();
        assertEquals(InterfaceProvisionRelation.class, implication.getClass());
        InterfaceProvisionRelation relation = (InterfaceProvisionRelation) implication;
        assertEquals(element, relation.getDestination());
        assertTrue(relation.isPlaceholder());
        assertTrue(relation.getSource().isPlaceholder());
    }

    @Override
    protected InterfaceProcessor createProcessor(PcmSurrogate model) {
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
