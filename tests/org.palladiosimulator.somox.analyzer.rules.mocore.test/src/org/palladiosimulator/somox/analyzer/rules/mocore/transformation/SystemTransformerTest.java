package org.palladiosimulator.somox.analyzer.rules.mocore.transformation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.palladiosimulator.somox.analyzer.rules.mocore.utility.PcmEvaluationUtility.containsRepresentative;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Component;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Interface;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.ComponentAssemblyRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.InterfaceProvisionRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.InterfaceRequirementRelation;

import com.gstuer.modelmerging.framework.transformation.TransformerTest;

public class SystemTransformerTest extends TransformerTest<SystemTransformer, PcmSurrogate, System> {
    @Test
    public void testTransformSingleComponent() {
        // Test data
        SystemTransformer transformer = createTransformer();
        PcmSurrogate model = createEmptyModel();
        Component component = Component.getUniquePlaceholder();

        model.add(component);

        // Execution
        System system = transformer.transform(model);

        // Assertion
        assertNotNull(system);
        assertTrue(containsRepresentative(system, component));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void testTransformSingleAssemblyRelation(boolean isPlaceholderAssembly) {
        // Test data
        SystemTransformer transformer = createTransformer();
        PcmSurrogate model = createEmptyModel();

        Component provider = Component.getUniquePlaceholder();
        Component consumer = Component.getUniquePlaceholder();
        Interface providerConsumerInterface = Interface.getUniquePlaceholder();
        InterfaceProvisionRelation provisionRelation = new InterfaceProvisionRelation(provider,
                providerConsumerInterface, false);
        InterfaceRequirementRelation requirementRelation = new InterfaceRequirementRelation(consumer,
                providerConsumerInterface, false);
        ComponentAssemblyRelation assemblyRelation = new ComponentAssemblyRelation(provisionRelation,
                requirementRelation, isPlaceholderAssembly);

        model.add(provider);
        model.add(consumer);
        model.add(providerConsumerInterface);
        model.add(provisionRelation);
        model.add(requirementRelation);
        model.add(assemblyRelation);

        // Execution
        System system = transformer.transform(model);

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
