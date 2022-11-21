package com.gstuer.modelmerging.instance.pcm.processor.element;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import com.gstuer.modelmerging.framework.processor.ProcessorTest;
import com.gstuer.modelmerging.framework.surrogate.Replaceable;
import com.gstuer.modelmerging.instance.pcm.surrogate.PcmSurrogate;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.ServiceEffectSpecification;

public class ServiceEffectSpecificationProcessorTest
        extends ProcessorTest<ServiceEffectSpecificationProcessor, PcmSurrogate, ServiceEffectSpecification> {
    @Test
    @DisabledIf(TEST_API_ONLY_METHOD_NAME)
    public void testRefineWithValidElementAddsCorrectImplications() {
        // Test data
        PcmSurrogate model = createEmptyModel();
        ServiceEffectSpecificationProcessor processor = createProcessor(model);
        ServiceEffectSpecification element = createUniqueReplaceable();

        // Assertions: Pre-execution
        assertTrue(processor.getImplications().isEmpty());

        // Execution
        processor.refine(element);
        Set<Replaceable> implications = processor.getImplications();

        // Assertions: Post-execution
        assertEquals(0, implications.size());
    }

    @Override
    protected ServiceEffectSpecificationProcessor createProcessor(PcmSurrogate model) {
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
