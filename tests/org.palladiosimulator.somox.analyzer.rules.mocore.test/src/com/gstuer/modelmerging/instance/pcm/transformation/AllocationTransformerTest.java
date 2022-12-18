package com.gstuer.modelmerging.instance.pcm.transformation;

import static com.gstuer.modelmerging.instance.pcm.utility.PcmEvaluationUtility.containsRepresentative;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.palladiosimulator.pcm.allocation.Allocation;

import com.gstuer.modelmerging.framework.transformation.TransformerTest;
import com.gstuer.modelmerging.instance.pcm.surrogate.PcmSurrogate;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Component;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Deployment;
import com.gstuer.modelmerging.instance.pcm.surrogate.relation.ComponentAllocationRelation;

public class AllocationTransformerTest extends TransformerTest<AllocationTransformer, PcmSurrogate, Allocation> {
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void testTransformSingleAllocation(boolean isPlaceholderAllocation) {
        // Test data
        AllocationTransformer transformer = createTransformer();
        PcmSurrogate model = createEmptyModel();
        Component component = Component.getUniquePlaceholder();
        Deployment deployment = Deployment.getUniquePlaceholder();
        ComponentAllocationRelation allocationRelation = new ComponentAllocationRelation(component,
                deployment, isPlaceholderAllocation);

        model.add(component);
        model.add(deployment);
        model.add(allocationRelation);

        // Execution
        Allocation allocation = transformer.transform(model);

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
