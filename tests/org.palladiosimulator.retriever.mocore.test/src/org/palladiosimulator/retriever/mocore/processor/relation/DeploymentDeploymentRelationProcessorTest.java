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
import org.palladiosimulator.retriever.mocore.surrogate.relation.LinkResourceSpecificationRelation;
import org.palladiosimulator.retriever.mocore.utility.ElementFactory;

import tools.mdsd.mocore.framework.processor.RelationProcessorTest;
import tools.mdsd.mocore.framework.surrogate.Replaceable;

public class DeploymentDeploymentRelationProcessorTest extends
        RelationProcessorTest<DeploymentDeploymentRelationProcessor, PcmSurrogate, DeploymentDeploymentRelation, Deployment, Deployment> {
    @Test
    @DisabledIf(TEST_API_ONLY_METHOD_NAME)
    public void testRefineWithValidElementAddsCorrectImplications() {
        // Test data
        final PcmSurrogate model = this.createEmptyModel();
        final DeploymentDeploymentRelationProcessor processor = this.createProcessor(model);
        final DeploymentDeploymentRelation relation = this.createUniqueReplaceable();

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

        //// Implicit LinkResourceSpecificationRelation
        assertEquals(1, implications.size());
        final Replaceable implication = implications.stream()
            .findFirst()
            .orElseThrow();
        assertEquals(LinkResourceSpecificationRelation.class, implication.getClass());
        final LinkResourceSpecificationRelation implicitSpecification = (LinkResourceSpecificationRelation) implication;
        assertEquals(relation, implicitSpecification.getDestination());
        assertTrue(implicitSpecification.isPlaceholder());
        assertTrue(implicitSpecification.getSource()
            .isPlaceholder());
    }

    @Test
    @DisabledIf(TEST_API_ONLY_METHOD_NAME)
    public void testRefineDoesNotAddAssemblyIfParallelExists() {
        // Test data
        final PcmSurrogate model = this.createEmptyModel();
        final DeploymentDeploymentRelationProcessor processor = this.createProcessor(model);
        final DeploymentDeploymentRelation relation = this.createUniqueReplaceable();

        final Component<?> provider = Component.getUniquePlaceholder();
        final Component<?> consumer = Component.getUniquePlaceholder();
        final Interface interfc = Interface.getUniquePlaceholder();
        final InterfaceProvisionRelation interfaceProvision = new InterfaceProvisionRelation(provider, interfc, false);
        final InterfaceRequirementRelation interfaceRequirement = new InterfaceRequirementRelation(consumer, interfc,
                false);
        final ComponentAssemblyRelation assembly = new ComponentAssemblyRelation(interfaceProvision,
                interfaceRequirement, false);

        final ComponentAllocationRelation providerAllocation = new ComponentAllocationRelation(provider,
                relation.getSource(), false);
        final ComponentAllocationRelation consumerAllocation = new ComponentAllocationRelation(consumer,
                relation.getDestination(), false);

        model.add(assembly);
        model.add(providerAllocation);
        model.add(consumerAllocation);

        // Assertions: Pre-execution
        assertTrue(processor.getImplications()
            .isEmpty());

        // Execution
        processor.refine(relation);
        final Set<Replaceable> implications = new HashSet<>(processor.getImplications());

        // Assertions: Post-execution
        assertTrue(implications.remove(relation.getSource()));
        assertTrue(implications.remove(relation.getDestination()));
        assertTrue(implications.removeIf(implication -> implication instanceof LinkResourceSpecificationRelation));
        assertEquals(0, implications.size());

        //// ComponentAssemblyRelation stays untouched
        assertTrue(model.contains(assembly));
        assertTrue(model.contains(providerAllocation));
        assertTrue(model.contains(consumerAllocation));
    }

    @Test
    @DisabledIf(TEST_API_ONLY_METHOD_NAME)
    public void testRefineDoesNotAddAssemblyIfInverseExists() {
        // Test data
        final PcmSurrogate model = this.createEmptyModel();
        final DeploymentDeploymentRelationProcessor processor = this.createProcessor(model);
        final DeploymentDeploymentRelation relation = this.createUniqueReplaceable();

        final Component<?> provider = Component.getUniquePlaceholder();
        final Component<?> consumer = Component.getUniquePlaceholder();
        final Interface interfc = Interface.getUniquePlaceholder();
        final InterfaceProvisionRelation interfaceProvision = new InterfaceProvisionRelation(provider, interfc, false);
        final InterfaceRequirementRelation interfaceRequirement = new InterfaceRequirementRelation(consumer, interfc,
                false);
        final ComponentAssemblyRelation assembly = new ComponentAssemblyRelation(interfaceProvision,
                interfaceRequirement, false);

        final ComponentAllocationRelation providerAllocation = new ComponentAllocationRelation(consumer,
                relation.getSource(), false);
        final ComponentAllocationRelation consumerAllocation = new ComponentAllocationRelation(provider,
                relation.getDestination(), false);

        model.add(assembly);
        model.add(providerAllocation);
        model.add(consumerAllocation);

        // Assertions: Pre-execution
        assertTrue(processor.getImplications()
            .isEmpty());

        // Execution
        processor.refine(relation);
        final Set<Replaceable> implications = new HashSet<>(processor.getImplications());

        // Assertions: Post-execution
        assertTrue(implications.remove(relation.getSource()));
        assertTrue(implications.remove(relation.getDestination()));
        assertTrue(implications.removeIf(implication -> implication instanceof LinkResourceSpecificationRelation));
        assertEquals(0, implications.size());

        //// ComponentAssemblyRelation stays untouched
        assertTrue(model.contains(assembly));
        assertTrue(model.contains(providerAllocation));
        assertTrue(model.contains(consumerAllocation));
    }

    @Override
    @Test
    public void testProcessReplacesIndirectPlaceholder() {
        // Test data
        final PcmSurrogate model = this.createEmptyModel();
        final DeploymentDeploymentRelationProcessor processor = this.createProcessor(model);
        final Deployment source = this.getUniqueNonPlaceholderSourceEntity();
        final Deployment destination = this.getUniqueNonPlaceholderDestinationEntity();
        final Deployment destinationPlaceholder = this.getPlaceholderOfDestinationEntity(destination);
        final DeploymentDeploymentRelation relation = this.createRelation(source, destination, true);
        final DeploymentDeploymentRelation placeholder = this.createRelation(source, destinationPlaceholder, true);

        // Execution & Assertions to add placeholder
        model.add(placeholder);
        model.add(source);
        model.add(destinationPlaceholder);
        assertTrue(model.contains(placeholder));
        assertTrue(model.contains(source));
        assertTrue(model.contains(destinationPlaceholder));
        assertFalse(model.contains(destination));
        assertFalse(model.contains(relation));
        assertTrue(processor.getImplications()
            .isEmpty());

        // Execution to replace placeholder
        processor.process(relation);
        final Set<Replaceable> implications = processor.getImplications();

        // Assertions - Model State
        assertTrue(model.contains(placeholder));
        assertTrue(model.contains(source));
        assertTrue(model.contains(destinationPlaceholder));
        assertFalse(model.contains(destination));
        assertTrue(model.contains(relation));

        // Assertions - Implications
        assertFalse(implications.contains(placeholder));
        assertTrue(implications.contains(source));
        assertFalse(implications.contains(destinationPlaceholder));
        assertTrue(implications.contains(destination));
        assertFalse(implications.contains(relation));
    }

    @Override
    @Test
    @DisabledIf(TEST_API_ONLY_METHOD_NAME)
    public void testReplaceIndirectPlaceholdersSameSource() {
        // Test data
        final PcmSurrogate model = this.createEmptyModel();
        final DeploymentDeploymentRelationProcessor processor = this.createProcessor(model);
        final Deployment source = this.getUniqueNonPlaceholderSourceEntity();
        final Deployment destination = this.getUniqueNonPlaceholderDestinationEntity();
        final Deployment destinationPlaceholder = this.getPlaceholderOfDestinationEntity(destination);
        final DeploymentDeploymentRelation relation = this.createRelation(source, destination, true);
        final DeploymentDeploymentRelation placeholder = this.createRelation(source, destinationPlaceholder, true);

        // Execution & Assertions to add placeholder
        model.add(placeholder);
        model.add(source);
        model.add(destinationPlaceholder);
        assertTrue(model.contains(placeholder));
        assertTrue(model.contains(source));
        assertTrue(model.contains(destinationPlaceholder));
        assertFalse(model.contains(destination));
        assertFalse(model.contains(relation));
        assertTrue(processor.getImplications()
            .isEmpty());

        // Execution to replace placeholder
        processor.replaceIndirectPlaceholders(relation);
        final Set<Replaceable> implications = processor.getImplications();

        // Assertions - Model State
        assertTrue(model.contains(placeholder));
        assertTrue(model.contains(source));
        assertTrue(model.contains(destinationPlaceholder));
        assertFalse(model.contains(destination));
        assertFalse(model.contains(relation));

        // Assertions - Implications
        assertFalse(implications.contains(placeholder));
        assertFalse(implications.contains(source));
        assertFalse(implications.contains(destinationPlaceholder));
        assertFalse(implications.contains(destination));
        assertFalse(implications.contains(relation));
    }

    @Override
    @Test
    @DisabledIf(TEST_API_ONLY_METHOD_NAME)
    public void testReplaceIndirectPlaceholdersSameDestination() {
        // Test data
        final PcmSurrogate model = this.createEmptyModel();
        final DeploymentDeploymentRelationProcessor processor = this.createProcessor(model);
        final Deployment source = this.getUniqueNonPlaceholderSourceEntity();
        final Deployment sourcePlaceholder = this.getPlaceholderOfSourceEntity(source);
        final Deployment destination = this.getUniqueNonPlaceholderDestinationEntity();
        final DeploymentDeploymentRelation relation = this.createRelation(source, destination, true);
        final DeploymentDeploymentRelation placeholder = this.createRelation(sourcePlaceholder, destination, true);

        // Execution & Assertions to add placeholder
        model.add(placeholder);
        model.add(sourcePlaceholder);
        model.add(destination);
        assertTrue(model.contains(placeholder));
        assertTrue(model.contains(sourcePlaceholder));
        assertTrue(model.contains(destination));
        assertFalse(model.contains(source));
        assertFalse(model.contains(relation));
        assertTrue(processor.getImplications()
            .isEmpty());

        // Execution to replace placeholder
        processor.replaceIndirectPlaceholders(relation);
        final Set<Replaceable> implications = processor.getImplications();

        // Assertions - Model State
        assertTrue(model.contains(placeholder));
        assertTrue(model.contains(sourcePlaceholder));
        assertTrue(model.contains(destination));
        assertFalse(model.contains(source));
        assertFalse(model.contains(relation));

        // Assertions - Implications
        assertFalse(implications.contains(placeholder));
        assertFalse(implications.contains(sourcePlaceholder));
        assertFalse(implications.contains(destination));
        assertFalse(implications.contains(source));
        assertFalse(implications.contains(relation));
    }

    @Override
    protected DeploymentDeploymentRelation createRelation(final Deployment source, final Deployment destination,
            final boolean isPlaceholder) {
        return new DeploymentDeploymentRelation(source, destination, isPlaceholder);
    }

    @Override
    protected Deployment getUniqueNonPlaceholderSourceEntity() {
        return ElementFactory.createUniqueDeployment(false);
    }

    @Override
    protected Deployment getPlaceholderOfSourceEntity(final Deployment source) {
        return new Deployment(source.getValue(), true);
    }

    @Override
    protected Deployment getUniqueNonPlaceholderDestinationEntity() {
        return this.getUniqueNonPlaceholderSourceEntity();
    }

    @Override
    protected Deployment getPlaceholderOfDestinationEntity(final Deployment destination) {
        return this.getPlaceholderOfSourceEntity(destination);
    }

    @Override
    protected DeploymentDeploymentRelationProcessor createProcessor(final PcmSurrogate model) {
        return new DeploymentDeploymentRelationProcessor(model);
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }
}
