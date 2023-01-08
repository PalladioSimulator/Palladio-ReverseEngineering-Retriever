package org.palladiosimulator.somox.analyzer.rules.mocore.transformation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.palladiosimulator.somox.analyzer.rules.mocore.utility.PcmEvaluationUtility.containsRepresentative;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Component;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Deployment;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.ComponentAllocationRelation;

import tools.mdsd.mocore.framework.transformation.TransformerTest;

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
