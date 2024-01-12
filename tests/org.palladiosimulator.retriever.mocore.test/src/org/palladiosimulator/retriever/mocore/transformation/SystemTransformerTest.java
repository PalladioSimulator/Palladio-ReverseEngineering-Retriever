package org.palladiosimulator.retriever.mocore.transformation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.palladiosimulator.retriever.mocore.utility.PcmEvaluationUtility.containsRepresentative;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.Component;
import org.palladiosimulator.retriever.mocore.surrogate.element.Interface;
import org.palladiosimulator.retriever.mocore.surrogate.relation.ComponentAssemblyRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.InterfaceProvisionRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.InterfaceRequirementRelation;

import tools.mdsd.mocore.framework.transformation.TransformerTest;

public class SystemTransformerTest extends TransformerTest<SystemTransformer, PcmSurrogate, System> {
    @Test
    public void testTransformSingleComponent() {
        // Test data
        final SystemTransformer transformer = this.createTransformer();
        final PcmSurrogate model = this.createEmptyModel();
        final Component<?> component = Component.getUniquePlaceholder();

        model.add(component);

        // Execution
        final System system = transformer.transform(model);

        // Assertion
        assertNotNull(system);
        assertTrue(containsRepresentative(system, component));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void testTransformSingleAssemblyRelation(final boolean isPlaceholderAssembly) {
        // Test data
        final SystemTransformer transformer = this.createTransformer();
        final PcmSurrogate model = this.createEmptyModel();

        final Component<?> provider = Component.getUniquePlaceholder();
        final Component<?> consumer = Component.getUniquePlaceholder();
        final Interface providerConsumerInterface = Interface.getUniquePlaceholder();
        final InterfaceProvisionRelation provisionRelation = new InterfaceProvisionRelation(provider,
                providerConsumerInterface, false);
        final InterfaceRequirementRelation requirementRelation = new InterfaceRequirementRelation(consumer,
                providerConsumerInterface, false);
        final ComponentAssemblyRelation assemblyRelation = new ComponentAssemblyRelation(provisionRelation,
                requirementRelation, isPlaceholderAssembly);

        model.add(provider);
        model.add(consumer);
        model.add(providerConsumerInterface);
        model.add(provisionRelation);
        model.add(requirementRelation);
        model.add(assemblyRelation);

        // Execution
        final System system = transformer.transform(model);

        // Assertion
        assertNotNull(system);
        assertTrue(containsRepresentative(system, provider));
        assertTrue(containsRepresentative(system, consumer));
        assertTrue(containsRepresentative(system, assemblyRelation));
    }

    @Override
    protected SystemTransformer createTransformer() {
        return new SystemTransformer();
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }
}
