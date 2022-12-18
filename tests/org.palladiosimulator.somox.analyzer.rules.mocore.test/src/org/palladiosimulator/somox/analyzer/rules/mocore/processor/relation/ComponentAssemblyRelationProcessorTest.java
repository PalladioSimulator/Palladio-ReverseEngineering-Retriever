package org.palladiosimulator.somox.analyzer.rules.mocore.processor.relation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.palladiosimulator.somox.analyzer.rules.mocore.processor.relation.ComponentAssemblyRelationProcessor;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Component;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Deployment;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Interface;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.ComponentAllocationRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.ComponentAssemblyRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.DeploymentDeploymentRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.InterfaceProvisionRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.InterfaceRequirementRelation;

import com.gstuer.modelmerging.framework.processor.RelationProcessorTest;
import com.gstuer.modelmerging.framework.surrogate.Replaceable;

public class ComponentAssemblyRelationProcessorTest extends RelationProcessorTest<ComponentAssemblyRelationProcessor,
        PcmSurrogate, ComponentAssemblyRelation, InterfaceProvisionRelation, InterfaceRequirementRelation> {
    private static final Interface RELATION_DESTINATION = Interface.getUniquePlaceholder();

    @Test
    @DisabledIf(TEST_API_ONLY_METHOD_NAME)
    public void testRefinementRemovesParallelAssemblyPlaceholder() {
        // Test data
        PcmSurrogate model = createEmptyModel();
        ComponentAssemblyRelationProcessor processor = createProcessor(model);

        InterfaceProvisionRelation interfaceProvision = getUniqueNonPlaceholderSourceEntity();
        InterfaceRequirementRelation interfaceRequirement = getUniqueNonPlaceholderDestinationEntity();
        ComponentAssemblyRelation relation = createRelation(interfaceProvision, interfaceRequirement, false);

        Deployment providingContainer = Deployment.getUniquePlaceholder();
        Deployment requiringContainer = Deployment.getUniquePlaceholder();
        ComponentAllocationRelation providingAllocation = new ComponentAllocationRelation(
                interfaceProvision.getSource(), providingContainer, false);
        ComponentAllocationRelation requiringAllocation = new ComponentAllocationRelation(
                interfaceRequirement.getSource(), requiringContainer, false);

        InterfaceProvisionRelation placeholderProvision = getPlaceholderOfSourceEntity(
                getUniqueNonPlaceholderSourceEntity());
        InterfaceRequirementRelation placeholderRequirement = getPlaceholderOfDestinationEntity(
                getUniqueNonPlaceholderDestinationEntity());
        ComponentAllocationRelation placeholderProvidingAllocation = new ComponentAllocationRelation(
                placeholderProvision.getSource(), providingContainer, false);
        ComponentAllocationRelation placeholderRequiringAllocation = new ComponentAllocationRelation(
                placeholderRequirement.getSource(), requiringContainer, false);
        ComponentAssemblyRelation placeholderRelation = createRelation(placeholderProvision,
                placeholderRequirement, true);

        // Add containers, placeholder assembly & allocations to model
        model.add(providingContainer);
        model.add(requiringContainer);
        model.add(providingAllocation);
        model.add(requiringAllocation);

        model.add(placeholderProvision.getSource());
        model.add(placeholderProvision.getDestination());
        model.add(placeholderRequirement.getSource());
        model.add(placeholderProvision);
        model.add(placeholderRequirement);
        model.add(placeholderProvidingAllocation);
        model.add(placeholderRequiringAllocation);
        model.add(placeholderRelation);

        // Assertions: Pre-execution
        assertTrue(processor.getImplications().isEmpty());
        assertTrue(model.contains(placeholderProvision.getSource()));
        assertTrue(model.contains(placeholderProvision.getDestination()));
        assertTrue(model.contains(placeholderRequirement.getSource()));
        assertTrue(model.contains(placeholderProvision));
        assertTrue(model.contains(placeholderRequirement));
        assertTrue(model.contains(placeholderProvidingAllocation));
        assertTrue(model.contains(placeholderRequiringAllocation));
        assertTrue(model.contains(placeholderRelation));

        // Execution
        processor.refine(relation);
        Set<Replaceable> implications = new HashSet<>(processor.getImplications());

        // Assertions: Post-execution
        assertFalse(model.contains(placeholderProvision.getSource()));
        assertFalse(model.contains(placeholderProvision.getDestination()));
        assertFalse(model.contains(placeholderRequirement.getSource()));
        assertFalse(model.contains(placeholderProvision));
        assertFalse(model.contains(placeholderRequirement));
        assertFalse(model.contains(placeholderProvidingAllocation));
        assertFalse(model.contains(placeholderRequiringAllocation));
        assertFalse(model.contains(placeholderRelation));

        assertTrue(implications.contains(relation));
        assertTrue(implications.contains(interfaceProvision.getSource()));
        assertTrue(implications.contains(interfaceProvision.getDestination()));
        assertTrue(implications.contains(interfaceRequirement.getSource()));
    }

    @Test
    @DisabledIf(TEST_API_ONLY_METHOD_NAME)
    public void testRefinementAddsImplicitDeploymentRelation() {
        // Test data
        PcmSurrogate model = createEmptyModel();
        ComponentAssemblyRelationProcessor processor = createProcessor(model);

        InterfaceProvisionRelation interfaceProvision = getUniqueNonPlaceholderSourceEntity();
        InterfaceRequirementRelation interfaceRequirement = getUniqueNonPlaceholderDestinationEntity();
        ComponentAssemblyRelation relation = createRelation(interfaceProvision, interfaceRequirement, false);

        Deployment providingContainer = Deployment.getUniquePlaceholder();
        Deployment requiringContainer = Deployment.getUniquePlaceholder();
        ComponentAllocationRelation providingAllocation = new ComponentAllocationRelation(
                interfaceProvision.getSource(), providingContainer, false);
        ComponentAllocationRelation requiringAllocation = new ComponentAllocationRelation(
                interfaceRequirement.getSource(), requiringContainer, false);

        // Add containers & allocations to model
        model.add(providingContainer);
        model.add(requiringContainer);
        model.add(providingAllocation);
        model.add(requiringAllocation);

        // Assertions: Pre-execution
        assertTrue(processor.getImplications().isEmpty());

        // Execution
        processor.refine(relation);
        Set<Replaceable> implications = new HashSet<>(processor.getImplications());

        // Assertions: Post-execution
        assertTrue(implications.remove(relation.getSource()));
        assertTrue(implications.remove(relation.getDestination()));
        assertEquals(1, implications.size());
        Replaceable implication = implications.stream().findFirst().orElseThrow();
        assertEquals(DeploymentDeploymentRelation.class, implication.getClass());
        DeploymentDeploymentRelation implicitDeploymentLink = (DeploymentDeploymentRelation) implication;
        assertEquals(providingContainer, implicitDeploymentLink.getSource());
        assertEquals(requiringContainer, implicitDeploymentLink.getDestination());
        assertTrue(implicitDeploymentLink.isPlaceholder());
    }

    @Override
    protected ComponentAssemblyRelation createRelation(InterfaceProvisionRelation source,
            InterfaceRequirementRelation destination,
            boolean isPlaceholder) {
        return new ComponentAssemblyRelation(source, destination, isPlaceholder);
    }

    @Override
    protected InterfaceProvisionRelation getUniqueNonPlaceholderSourceEntity() {
        Component source = Component.getUniquePlaceholder();
        return new InterfaceProvisionRelation(source, RELATION_DESTINATION, false);
    }

    @Override
    protected InterfaceProvisionRelation getPlaceholderOfSourceEntity(InterfaceProvisionRelation source) {
        return new InterfaceProvisionRelation(source.getSource(), source.getDestination(), true);
    }

    @Override
    protected InterfaceRequirementRelation getUniqueNonPlaceholderDestinationEntity() {
        Component source = Component.getUniquePlaceholder();
        return new InterfaceRequirementRelation(source, RELATION_DESTINATION, false);
    }

    @Override
    protected InterfaceRequirementRelation getPlaceholderOfDestinationEntity(InterfaceRequirementRelation destination) {
        return new InterfaceRequirementRelation(destination.getSource(), destination.getDestination(), true);
    }

    @Override
    protected ComponentAssemblyRelationProcessor createProcessor(PcmSurrogate model) {
        return new ComponentAssemblyRelationProcessor(model);
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }
}
