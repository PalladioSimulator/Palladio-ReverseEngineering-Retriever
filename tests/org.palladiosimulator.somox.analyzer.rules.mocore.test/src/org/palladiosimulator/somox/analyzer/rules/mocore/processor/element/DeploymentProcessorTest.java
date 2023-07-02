package org.palladiosimulator.somox.analyzer.rules.mocore.processor.element;

import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Deployment;

import tools.mdsd.mocore.framework.processor.ProcessorTest;

public class DeploymentProcessorTest extends ProcessorTest<DeploymentProcessor, PcmSurrogate, Deployment> {
    @Test
    @DisabledIf(TEST_API_ONLY_METHOD_NAME)
    public void testRefineWithValidElementAddsCorrectImplications() {
        // Test data
        PcmSurrogate model = createEmptyModel();
        DeploymentProcessor processor = createProcessor(model);
        Deployment element = createUniqueReplaceable();

        // Assertions: Pre-execution
        assertTrue(processor.getImplications().isEmpty());

        // Execution
        processor.refine(element);

        // Assertions: Post-execution
        assertTrue(processor.getImplications().isEmpty());
    }

    @Override
    protected DeploymentProcessor createProcessor(PcmSurrogate model) {
        return new DeploymentProcessor(model);
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }

    @Override
    protected Deployment createUniqueReplaceable() {
        return Deployment.getUniquePlaceholder();
    }
}
