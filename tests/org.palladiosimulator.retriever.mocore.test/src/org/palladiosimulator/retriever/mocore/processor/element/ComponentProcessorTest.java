package org.palladiosimulator.retriever.mocore.processor.element;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.Component;
import org.palladiosimulator.retriever.mocore.surrogate.relation.ComponentAllocationRelation;

import tools.mdsd.mocore.framework.processor.ProcessorTest;
import tools.mdsd.mocore.framework.surrogate.Replaceable;

public abstract class ComponentProcessorTest<T extends Component<?>>
        extends ProcessorTest<ComponentProcessor<T>, PcmSurrogate, T> {
    @Test
    @DisabledIf(TEST_API_ONLY_METHOD_NAME)
    public void testRefineWithValidElementAddsCorrectImplications() {
        // Test data
        final PcmSurrogate model = this.createEmptyModel();
        final ComponentProcessor<T> processor = this.createProcessor(model);
        final T element = this.createUniqueReplaceable();

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
        assertEquals(ComponentAllocationRelation.class, implication.getClass());
        final ComponentAllocationRelation relation = (ComponentAllocationRelation) implication;
        assertEquals(element, relation.getSource());
        assertTrue(relation.isPlaceholder());
        assertTrue(relation.getDestination()
            .isPlaceholder());
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }
}
