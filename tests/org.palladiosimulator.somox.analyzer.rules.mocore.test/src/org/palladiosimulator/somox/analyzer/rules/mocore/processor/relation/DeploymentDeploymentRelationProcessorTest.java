package org.palladiosimulator.somox.analyzer.rules.mocore.processor.relation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Component;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Deployment;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Interface;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.ComponentAllocationRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.ComponentAssemblyRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.DeploymentDeploymentRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.InterfaceProvisionRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.InterfaceRequirementRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.LinkResourceSpecificationRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.utility.ElementFactory;

import tools.mdsd.mocore.framework.processor.RelationProcessorTest;
import tools.mdsd.mocore.framework.surrogate.Replaceable;

public class DeploymentDeploymentRelationProcessorTest
        extends RelationProcessorTest<DeploymentDeploymentRelationProcessor,
                PcmSurrogate, DeploymentDeploymentRelation, Deployment, Deployment> {
    @Test
    @DisabledIf(TEST_API_ONLY_METHOD_NAME)
    public void testRefineWithValidElementAddsCorrectImplications() {
        // Test data
        PcmSurrogate model = createEmptyModel();
        DeploymentDeploymentRelationProcessor processor = createProcessor(model);
        DeploymentDeploymentRelation relation = createUniqueReplaceable();

        // Assertions: Pre-execution
        assertTrue(processor.getImplications().isEmpty());

        // Execution
        processor.refine(relation);
        Set<Replaceable> implications = new HashSet<>(processor.getImplications());

        // Assertions: Post-execution
        assertTrue(implications.remove(relation.getSource()));
        assertTrue(implications.remove(relation.getDestination()));
        assertEquals(1, implications.size());

        //// Implicit LinkResourceSpecificationRelation
        assertEquals(1, implications.size());
        Replaceable implication = implications.stream().findFirst().orElseThrow();
        assertEquals(LinkResourceSpecificationRelation.class, implication.getClass());
        LinkResourceSpecificationRelation implicitSpecification = (LinkResourceSpecificationRelation) implication;
        assertEquals(relation, implicitSpecification.getDestination());
        assertTrue(implicitSpecification.isPlaceholder());
        assertTrue(implicitSpecification.getSource().isPlaceholder());
    }

    @Test
    @DisabledIf(TEST_API_ONLY_METHOD_NAME)
    public void testRefineDoesNotAddAssemblyIfParallelExists() {
        // Test data
        PcmSurrogate model = createEmptyModel();
        DeploymentDeploymentRelationProcessor processor = createProcessor(model);
        DeploymentDeploymentRelation relation = createUniqueReplaceable();

        Component<?> provider = Component.getUniquePlaceholder();
        Component<?> consumer = Component.getUniquePlaceholder();
        Interface interfc = Interface.getUniquePlaceholder();
        InterfaceProvisionRelation interfaceProvision = new InterfaceProvisionRelation(provider, interfc, false);
        InterfaceRequirementRelation interfaceRequirement = new InterfaceRequirementRelation(consumer, interfc, false);
        ComponentAssemblyRelation assembly = new ComponentAssemblyRelation(interfaceProvision,
                interfaceRequirement, false);

        ComponentAllocationRelation providerAllocation = new ComponentAllocationRelation(provider,
                relation.getSource(), false);
        ComponentAllocationRelation consumerAllocation = new ComponentAllocationRelation(consumer,
                relation.getDestination(), false);

        model.add(assembly);
        model.add(providerAllocation);
        model.add(consumerAllocation);

        // Assertions: Pre-execution
        assertTrue(processor.getImplications().isEmpty());

        // Execution
        processor.refine(relation);
        Set<Replaceable> implications = new HashSet<>(processor.getImplications());

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
        PcmSurrogate model = createEmptyModel();
        DeploymentDeploymentRelationProcessor processor = createProcessor(model);
        DeploymentDeploymentRelation relation = createUniqueReplaceable();

        Component<?> provider = Component.getUniquePlaceholder();
        Component<?> consumer = Component.getUniquePlaceholder();
        Interface interfc = Interface.getUniquePlaceholder();
        InterfaceProvisionRelation interfaceProvision = new InterfaceProvisionRelation(provider, interfc, false);
        InterfaceRequirementRelation interfaceRequirement = new InterfaceRequirementRelation(consumer, interfc, false);
        ComponentAssemblyRelation assembly = new ComponentAssemblyRelation(interfaceProvision,
                interfaceRequirement, false);

        ComponentAllocationRelation providerAllocation = new ComponentAllocationRelation(consumer,
                relation.getSource(), false);
        ComponentAllocationRelation consumerAllocation = new ComponentAllocationRelation(provider,
                relation.getDestination(), false);

        model.add(assembly);
        model.add(providerAllocation);
        model.add(consumerAllocation);

        // Assertions: Pre-execution
        assertTrue(processor.getImplications().isEmpty());

        // Execution
        processor.refine(relation);
        Set<Replaceable> implications = new HashSet<>(processor.getImplications());

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
        PcmSurrogate model = createEmptyModel();
        DeploymentDeploymentRelationProcessor processor = createProcessor(model);
        Deployment source = getUniqueNonPlaceholderSourceEntity();
        Deployment destination = getUniqueNonPlaceholderDestinationEntity();
        Deployment destinationPlaceholder = getPlaceholderOfDestinationEntity(destination);
        DeploymentDeploymentRelation relation = createRelation(source, destination, true);
        DeploymentDeploymentRelation placeholder = createRelation(source, destinationPlaceholder, true);

        // Execution & Assertions to add placeholder
        model.add(placeholder);
        model.add(source);
        model.add(destinationPlaceholder);
        assertTrue(model.contains(placeholder));
        assertTrue(model.contains(source));
        assertTrue(model.contains(destinationPlaceholder));
        assertFalse(model.contains(destination));
        assertFalse(model.contains(relation));
        assertTrue(processor.getImplications().isEmpty());

        // Execution to replace placeholder
        processor.process(relation);
        Set<Replaceable> implications = processor.getImplications();

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
        PcmSurrogate model = createEmptyModel();
        DeploymentDeploymentRelationProcessor processor = createProcessor(model);
        Deployment source = getUniqueNonPlaceholderSourceEntity();
        Deployment destination = getUniqueNonPlaceholderDestinationEntity();
        Deployment destinationPlaceholder = getPlaceholderOfDestinationEntity(destination);
        DeploymentDeploymentRelation relation = createRelation(source, destination, true);
        DeploymentDeploymentRelation placeholder = createRelation(source, destinationPlaceholder, true);

        // Execution & Assertions to add placeholder
        model.add(placeholder);
        model.add(source);
        model.add(destinationPlaceholder);
        assertTrue(model.contains(placeholder));
        assertTrue(model.contains(source));
        assertTrue(model.contains(destinationPlaceholder));
        assertFalse(model.contains(destination));
        assertFalse(model.contains(relation));
        assertTrue(processor.getImplications().isEmpty());

        // Execution to replace placeholder
        processor.replaceIndirectPlaceholders(relation);
        Set<Replaceable> implications = processor.getImplications();

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
        PcmSurrogate model = createEmptyModel();
        DeploymentDeploymentRelationProcessor processor = createProcessor(model);
        Deployment source = getUniqueNonPlaceholderSourceEntity();
        Deployment sourcePlaceholder = getPlaceholderOfSourceEntity(source);
        Deployment destination = getUniqueNonPlaceholderDestinationEntity();
        DeploymentDeploymentRelation relation = createRelation(source, destination, true);
        DeploymentDeploymentRelation placeholder = createRelation(sourcePlaceholder, destination, true);

        // Execution & Assertions to add placeholder
        model.add(placeholder);
        model.add(sourcePlaceholder);
        model.add(destination);
        assertTrue(model.contains(placeholder));
        assertTrue(model.contains(sourcePlaceholder));
        assertTrue(model.contains(destination));
        assertFalse(model.contains(source));
        assertFalse(model.contains(relation));
        assertTrue(processor.getImplications().isEmpty());

        // Execution to replace placeholder
        processor.replaceIndirectPlaceholders(relation);
        Set<Replaceable> implications = processor.getImplications();

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
    protected DeploymentDeploymentRelation createRelation(Deployment source, Deployment destination,
            boolean isPlaceholder) {
        return new DeploymentDeploymentRelation(source, destination, isPlaceholder);
    }

    @Override
    protected Deployment getUniqueNonPlaceholderSourceEntity() {
        return ElementFactory.createUniqueDeployment(false);
    }

    @Override
    protected Deployment getPlaceholderOfSourceEntity(Deployment source) {
        return new Deployment(source.getValue(), true);
    }

    @Override
    protected Deployment getUniqueNonPlaceholderDestinationEntity() {
        return getUniqueNonPlaceholderSourceEntity();
    }

    @Override
    protected Deployment getPlaceholderOfDestinationEntity(Deployment destination) {
        return getPlaceholderOfSourceEntity(destination);
    }

    @Override
    protected DeploymentDeploymentRelationProcessor createProcessor(PcmSurrogate model) {
        return new DeploymentDeploymentRelationProcessor(model);
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }
}
