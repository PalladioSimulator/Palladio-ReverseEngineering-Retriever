package org.palladiosimulator.somox.analyzer.rules.mocore.processor.element;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.palladiosimulator.somox.analyzer.rules.mocore.processor.element.LinkResourceSpecificationProcessor;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.LinkResourceSpecification;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.LinkResourceSpecificationRelation;

import com.gstuer.modelmerging.framework.processor.ProcessorTest;
import com.gstuer.modelmerging.framework.surrogate.Replaceable;

public class LinkResourceSpecificationProcessorTest
        extends ProcessorTest<LinkResourceSpecificationProcessor, PcmSurrogate, LinkResourceSpecification> {
    @Test
    @DisabledIf(TEST_API_ONLY_METHOD_NAME)
    public void testRefineWithValidElementAddsCorrectImplications() {
        // Test data
        PcmSurrogate model = createEmptyModel();
        LinkResourceSpecificationProcessor processor = createProcessor(model);
        LinkResourceSpecification element = createUniqueReplaceable();

        // Assertions: Pre-execution
        assertTrue(processor.getImplications().isEmpty());

        // Execution
        processor.refine(element);
        Set<Replaceable> implications = processor.getImplications();

        // Assertions: Post-execution
        assertEquals(1, implications.size());
        Replaceable implication = implications.stream().findFirst().orElseThrow();
        assertEquals(LinkResourceSpecificationRelation.class, implication.getClass());
        LinkResourceSpecificationRelation relation = (LinkResourceSpecificationRelation) implication;
        assertEquals(element, relation.getSource());
        assertTrue(relation.isPlaceholder());
        assertTrue(relation.getDestination().isPlaceholder());
        assertTrue(relation.getDestination().getSource().isPlaceholder());
        assertTrue(relation.getDestination().getDestination().isPlaceholder());
    }

    @Override
    protected LinkResourceSpecificationProcessor createProcessor(PcmSurrogate model) {
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
