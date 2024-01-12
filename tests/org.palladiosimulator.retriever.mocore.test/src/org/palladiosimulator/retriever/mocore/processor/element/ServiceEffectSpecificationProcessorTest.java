package org.palladiosimulator.retriever.mocore.processor.element;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.ServiceEffectSpecification;

import tools.mdsd.mocore.framework.processor.ProcessorTest;
import tools.mdsd.mocore.framework.surrogate.Replaceable;

public class ServiceEffectSpecificationProcessorTest
        extends ProcessorTest<ServiceEffectSpecificationProcessor, PcmSurrogate, ServiceEffectSpecification> {
    @Test
    @DisabledIf(TEST_API_ONLY_METHOD_NAME)
    public void testRefineWithValidElementAddsCorrectImplications() {
        // Test data
        final PcmSurrogate model = this.createEmptyModel();
        final ServiceEffectSpecificationProcessor processor = this.createProcessor(model);
        final ServiceEffectSpecification element = this.createUniqueReplaceable();

        // Assertions: Pre-execution
        assertTrue(processor.getImplications()
            .isEmpty());

        // Execution
        processor.refine(element);
        final Set<Replaceable> implications = processor.getImplications();

        // Assertions: Post-execution
        assertEquals(0, implications.size());
    }

    @Override
    protected ServiceEffectSpecificationProcessor createProcessor(final PcmSurrogate model) {
        return new ServiceEffectSpecificationProcessor(model);
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }

    @Override
    protected ServiceEffectSpecification createUniqueReplaceable() {
        return ServiceEffectSpecification.getUniquePlaceholder();
    }

}
