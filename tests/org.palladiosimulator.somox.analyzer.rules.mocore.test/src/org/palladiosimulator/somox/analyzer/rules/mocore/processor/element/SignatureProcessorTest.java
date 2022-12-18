package org.palladiosimulator.somox.analyzer.rules.mocore.processor.element;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Signature;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.SignatureProvisionRelation;

import com.gstuer.modelmerging.framework.processor.ProcessorTest;
import com.gstuer.modelmerging.framework.surrogate.Replaceable;

public class SignatureProcessorTest extends ProcessorTest<SignatureProcessor, PcmSurrogate, Signature> {
    @Test
    @DisabledIf(TEST_API_ONLY_METHOD_NAME)
    public void testRefineWithValidElementAddsCorrectImplications() {
        // Test data
        PcmSurrogate model = createEmptyModel();
        SignatureProcessor processor = createProcessor(model);
        Signature element = createUniqueReplaceable();

        // Assertions: Pre-execution
        assertTrue(processor.getImplications().isEmpty());

        // Execution
        processor.refine(element);
        Set<Replaceable> implications = new HashSet<>(processor.getImplications());

        // Assertions: Post-execution
        //// Implicit providing interface
        assertEquals(1, implications.size());
        Replaceable implication = implications.stream().findFirst().orElseThrow();
        assertEquals(SignatureProvisionRelation.class, implication.getClass());
        SignatureProvisionRelation relation = (SignatureProvisionRelation) implication;
        assertEquals(element, relation.getSource());
        assertTrue(relation.isPlaceholder());
        assertTrue(relation.getDestination().isPlaceholder());
    }

    @Override
    protected SignatureProcessor createProcessor(PcmSurrogate model) {
        return new SignatureProcessor(model);
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }

    @Override
    protected Signature createUniqueReplaceable() {
        return Signature.getUniquePlaceholder();
    }
}
