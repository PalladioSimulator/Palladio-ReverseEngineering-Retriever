package org.palladiosimulator.retriever.mocore.processor.element;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.LinkResourceSpecification;
import org.palladiosimulator.retriever.mocore.surrogate.relation.LinkResourceSpecificationRelation;

import tools.mdsd.mocore.framework.processor.ProcessorTest;
import tools.mdsd.mocore.framework.surrogate.Replaceable;

public class LinkResourceSpecificationProcessorTest
        extends ProcessorTest<LinkResourceSpecificationProcessor, PcmSurrogate, LinkResourceSpecification> {
    @Test
    @DisabledIf(TEST_API_ONLY_METHOD_NAME)
    public void testRefineWithValidElementAddsCorrectImplications() {
        // Test data
        final PcmSurrogate model = this.createEmptyModel();
        final LinkResourceSpecificationProcessor processor = this.createProcessor(model);
        final LinkResourceSpecification element = this.createUniqueReplaceable();

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
        assertEquals(LinkResourceSpecificationRelation.class, implication.getClass());
        final LinkResourceSpecificationRelation relation = (LinkResourceSpecificationRelation) implication;
        assertEquals(element, relation.getSource());
        assertTrue(relation.isPlaceholder());
        assertTrue(relation.getDestination()
            .isPlaceholder());
        assertTrue(relation.getDestination()
            .getSource()
            .isPlaceholder());
        assertTrue(relation.getDestination()
            .getDestination()
            .isPlaceholder());
    }

    @Override
    protected LinkResourceSpecificationProcessor createProcessor(final PcmSurrogate model) {
        return new LinkResourceSpecificationProcessor(model);
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }

    @Override
    protected LinkResourceSpecification createUniqueReplaceable() {
        return LinkResourceSpecification.getUniquePlaceholder();
    }
}
