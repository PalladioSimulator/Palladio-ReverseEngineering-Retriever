package org.palladiosimulator.retriever.mocore.transformation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.palladiosimulator.retriever.mocore.utility.PcmEvaluationUtility.containsRepresentative;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.Component;
import org.palladiosimulator.retriever.mocore.surrogate.element.Deployment;
import org.palladiosimulator.retriever.mocore.surrogate.relation.ComponentAllocationRelation;

import tools.mdsd.mocore.framework.transformation.TransformerTest;

public class AllocationTransformerTest extends TransformerTest<AllocationTransformer, PcmSurrogate, Allocation> {
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void testTransformSingleAllocation(final boolean isPlaceholderAllocation) {
        // Test data
        final AllocationTransformer transformer = this.createTransformer();
        final PcmSurrogate model = this.createEmptyModel();
        final Component<?> component = Component.getUniquePlaceholder();
        final Deployment deployment = Deployment.getUniquePlaceholder();
        final ComponentAllocationRelation allocationRelation = new ComponentAllocationRelation(component, deployment,
                isPlaceholderAllocation);

        model.add(component);
        model.add(deployment);
        model.add(allocationRelation);

        // Execution
        final Allocation allocation = transformer.transform(model);

        // Assertion
        assertNotNull(allocation);
        assertTrue(containsRepresentative(allocation, allocationRelation));
    }

    @Override
    protected AllocationTransformer createTransformer() {
        return new AllocationTransformer();
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }
}
