package org.palladiosimulator.somox.analyzer.rules.mocore.processor.element;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Component;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.ComponentAllocationRelation;

import tools.mdsd.mocore.framework.processor.ProcessorTest;
import tools.mdsd.mocore.framework.surrogate.Replaceable;

public abstract class ComponentProcessorTest<T extends Component<?>>
        extends ProcessorTest<ComponentProcessor<T>, PcmSurrogate, T> {
    @Test
    @DisabledIf(TEST_API_ONLY_METHOD_NAME)
    public void testRefineWithValidElementAddsCorrectImplications() {
        // Test data
        PcmSurrogate model = createEmptyModel();
        ComponentProcessor<T> processor = createProcessor(model);
        T element = createUniqueReplaceable();

        // Assertions: Pre-execution
        assertTrue(processor.getImplications().isEmpty());

        // Execution
        processor.refine(element);
        Set<Replaceable> implications = processor.getImplications();

        // Assertions: Post-execution
        assertEquals(1, implications.size());
        Replaceable implication = implications.stream().findFirst().orElseThrow();
        assertEquals(ComponentAllocationRelation.class, implication.getClass());
        ComponentAllocationRelation relation = (ComponentAllocationRelation) implication;
        assertEquals(element, relation.getSource());
        assertTrue(relation.isPlaceholder());
        assertTrue(relation.getDestination().isPlaceholder());
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }
}
