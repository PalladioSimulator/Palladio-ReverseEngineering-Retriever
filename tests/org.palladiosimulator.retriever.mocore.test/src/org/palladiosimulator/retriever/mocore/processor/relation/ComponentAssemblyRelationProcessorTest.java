package org.palladiosimulator.retriever.mocore.processor.relation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.Component;
import org.palladiosimulator.retriever.mocore.surrogate.element.Deployment;
import org.palladiosimulator.retriever.mocore.surrogate.element.Interface;
import org.palladiosimulator.retriever.mocore.surrogate.relation.ComponentAllocationRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.ComponentAssemblyRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.DeploymentDeploymentRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.InterfaceProvisionRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.InterfaceRequirementRelation;

import tools.mdsd.mocore.framework.processor.RelationProcessorTest;
import tools.mdsd.mocore.framework.surrogate.Replaceable;

public class ComponentAssemblyRelationProcessorTest extends
        RelationProcessorTest<ComponentAssemblyRelationProcessor, PcmSurrogate, ComponentAssemblyRelation, InterfaceProvisionRelation, InterfaceRequirementRelation> {
    private static final Interface RELATION_DESTINATION = Interface.getUniquePlaceholder();

    @Test
    @DisabledIf(TEST_API_ONLY_METHOD_NAME)
    public void testRefinementRemovesParallelAssemblyPlaceholder() {
        // Test data
        final PcmSurrogate model = this.createEmptyModel();
        final ComponentAssemblyRelationProcessor processor = this.createProcessor(model);

        final InterfaceProvisionRelation interfaceProvision = this.getUniqueNonPlaceholderSourceEntity();
        final InterfaceRequirementRelation interfaceRequirement = this.getUniqueNonPlaceholderDestinationEntity();
        final ComponentAssemblyRelation relation = this.createRelation(interfaceProvision, interfaceRequirement, false);

        final Deployment providingContainer = Deployment.getUniquePlaceholder();
        final Deployment requiringContainer = Deployment.getUniquePlaceholder();
        final ComponentAllocationRelation providingAllocation = new ComponentAllocationRelation(
                interfaceProvision.getSource(), providingContainer, false);
        final ComponentAllocationRelation requiringAllocation = new ComponentAllocationRelation(
                interfaceRequirement.getSource(), requiringContainer, false);

        final InterfaceProvisionRelation placeholderProvision = this
            .getPlaceholderOfSourceEntity(this.getUniqueNonPlaceholderSourceEntity());
        final InterfaceRequirementRelation placeholderRequirement = this
            .getPlaceholderOfDestinationEntity(this.getUniqueNonPlaceholderDestinationEntity());
        final ComponentAllocationRelation placeholderProvidingAllocation = new ComponentAllocationRelation(
                placeholderProvision.getSource(), providingContainer, false);
        final ComponentAllocationRelation placeholderRequiringAllocation = new ComponentAllocationRelation(
                placeholderRequirement.getSource(), requiringContainer, false);
        final ComponentAssemblyRelation placeholderRelation = this.createRelation(placeholderProvision,
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
        assertTrue(processor.getImplications()
            .isEmpty());
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
        final Set<Replaceable> implications = new HashSet<>(processor.getImplications());

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
        final PcmSurrogate model = this.createEmptyModel();
        final ComponentAssemblyRelationProcessor processor = this.createProcessor(model);

        final InterfaceProvisionRelation interfaceProvision = this.getUniqueNonPlaceholderSourceEntity();
        final InterfaceRequirementRelation interfaceRequirement = this.getUniqueNonPlaceholderDestinationEntity();
        final ComponentAssemblyRelation relation = this.createRelation(interfaceProvision, interfaceRequirement, false);

        final Deployment providingContainer = Deployment.getUniquePlaceholder();
        final Deployment requiringContainer = Deployment.getUniquePlaceholder();
        final ComponentAllocationRelation providingAllocation = new ComponentAllocationRelation(
                interfaceProvision.getSource(), providingContainer, false);
        final ComponentAllocationRelation requiringAllocation = new ComponentAllocationRelation(
                interfaceRequirement.getSource(), requiringContainer, false);

        // Add containers & allocations to model
        model.add(providingContainer);
        model.add(requiringContainer);
        model.add(providingAllocation);
        model.add(requiringAllocation);

        // Assertions: Pre-execution
        assertTrue(processor.getImplications()
            .isEmpty());

        // Execution
        processor.refine(relation);
        final Set<Replaceable> implications = new HashSet<>(processor.getImplications());

        // Assertions: Post-execution
        assertTrue(implications.remove(relation.getSource()));
        assertTrue(implications.remove(relation.getDestination()));
        assertEquals(1, implications.size());
        final Replaceable implication = implications.stream()
            .findFirst()
            .orElseThrow();
        assertEquals(DeploymentDeploymentRelation.class, implication.getClass());
        final DeploymentDeploymentRelation implicitDeploymentLink = (DeploymentDeploymentRelation) implication;
        assertEquals(providingContainer, implicitDeploymentLink.getSource());
        assertEquals(requiringContainer, implicitDeploymentLink.getDestination());
        assertTrue(implicitDeploymentLink.isPlaceholder());
    }

    @Override
    protected ComponentAssemblyRelation createRelation(final InterfaceProvisionRelation source,
            final InterfaceRequirementRelation destination, final boolean isPlaceholder) {
        return new ComponentAssemblyRelation(source, destination, isPlaceholder);
    }

    @Override
    protected InterfaceProvisionRelation getUniqueNonPlaceholderSourceEntity() {
        final Component<?> source = Component.getUniquePlaceholder();
        return new InterfaceProvisionRelation(source, RELATION_DESTINATION, false);
    }

    @Override
    protected InterfaceProvisionRelation getPlaceholderOfSourceEntity(final InterfaceProvisionRelation source) {
        return new InterfaceProvisionRelation(source.getSource(), source.getDestination(), true);
    }

    @Override
    protected InterfaceRequirementRelation getUniqueNonPlaceholderDestinationEntity() {
        final Component<?> source = Component.getUniquePlaceholder();
        return new InterfaceRequirementRelation(source, RELATION_DESTINATION, false);
    }

    @Override
    protected InterfaceRequirementRelation getPlaceholderOfDestinationEntity(
            final InterfaceRequirementRelation destination) {
        return new InterfaceRequirementRelation(destination.getSource(), destination.getDestination(), true);
    }

    @Override
    protected ComponentAssemblyRelationProcessor createProcessor(final PcmSurrogate model) {
        return new ComponentAssemblyRelationProcessor(model);
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }
}
