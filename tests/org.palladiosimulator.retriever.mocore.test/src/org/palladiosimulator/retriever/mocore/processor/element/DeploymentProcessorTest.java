package org.palladiosimulator.retriever.mocore.processor.element;

import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.Deployment;

import tools.mdsd.mocore.framework.processor.ProcessorTest;

public class DeploymentProcessorTest extends ProcessorTest<DeploymentProcessor, PcmSurrogate, Deployment> {
    @Test
    @DisabledIf(TEST_API_ONLY_METHOD_NAME)
    public void testRefineWithValidElementAddsCorrectImplications() {
        // Test data
        final PcmSurrogate model = this.createEmptyModel();
        final DeploymentProcessor processor = this.createProcessor(model);
        final Deployment element = this.createUniqueReplaceable();

        // Assertions: Pre-execution
        assertTrue(processor.getImplications()
            .isEmpty());

        // Execution
        processor.refine(element);

        // Assertions: Post-execution
        assertTrue(processor.getImplications()
            .isEmpty());
    }

    @Override
    protected DeploymentProcessor createProcessor(final PcmSurrogate model) {
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
