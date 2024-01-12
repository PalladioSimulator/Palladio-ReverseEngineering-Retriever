package org.palladiosimulator.retriever.mocore.workflow;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.junit.jupiter.api.Test;
import org.palladiosimulator.generator.fluent.repository.api.Repo;
import org.palladiosimulator.generator.fluent.repository.factory.FluentRepositoryFactory;
import org.palladiosimulator.pcm.core.composition.AssemblyConnector;
import org.palladiosimulator.pcm.core.composition.Connector;
import org.palladiosimulator.pcm.core.composition.ProvidedDelegationConnector;
import org.palladiosimulator.pcm.core.composition.RequiredDelegationConnector;
import org.palladiosimulator.pcm.repository.CompositeComponent;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryComponent;
import org.palladiosimulator.pcm.system.System;

import de.uka.ipd.sdq.workflow.blackboard.Blackboard;

public class MoCoReJobTest {
    private final static String BLACKBOARD_OUTPUT_REPOSITORY = "input.repository";
    private final static String BLACKBOARD_INPUT_REPOSITORY = "output.repository";
    private final static String BLACKBOARD_OUTPUT_SYSTEM = "output.system";
    private final static String BLACKBOARD_OUTPUT_ALLOCATION = "output.allocation";
    private final static String BLACKBOARD_OUTPUT_RESOURCEENVIRONMENT = "output.resource";

    @Test
    public void testConstructorWithValidInput() {
        final Blackboard<Object> blackboard = new Blackboard<>();
        assertDoesNotThrow(() -> new MoCoReJob(blackboard, BLACKBOARD_INPUT_REPOSITORY, BLACKBOARD_OUTPUT_REPOSITORY,
                BLACKBOARD_OUTPUT_SYSTEM, BLACKBOARD_OUTPUT_ALLOCATION, BLACKBOARD_OUTPUT_RESOURCEENVIRONMENT));
    }

    @Test
    public void testCompositeComponentProcessing() throws Exception {
        // Tests constants
        final String componentNameOne = "Component One";
        final String contextNameOne = componentNameOne + " Context";
        final String componentNameTwo = "Component Two";
        final String contextNameTwo = componentNameTwo + " Context";
        final String interfaceNameInternal = "Internal Interface";
        final String roleNameInternalRequired = "Role Requirer " + interfaceNameInternal;
        final String roleNameInternalProvided = "Role Provider " + interfaceNameInternal;
        final String interfaceNameExternalRequired = "External Interface Required";
        final String roleNameExternalRequiredInner = "Inner Role " + interfaceNameExternalRequired;
        final String roleNameExternalRequiredOuter = "Outer Role " + interfaceNameExternalRequired;
        final String interfaceNameExternalProvided = "External Interface Provided";
        final String roleNameExternalProvidedInner = "Inner Role " + interfaceNameExternalProvided;
        final String roleNameExternalProvidedOuter = "Outer Role " + interfaceNameExternalProvided;

        // Create blackboard and fluent repository
        final Blackboard<Object> blackboard = new Blackboard<>();
        final FluentRepositoryFactory fluentFactory = new FluentRepositoryFactory();
        final Repo fluentRepository = fluentFactory.newRepository();

        // Create composite component and add to fluent repository
        fluentRepository.addToRepository(fluentFactory.newOperationInterface()
            .withName(interfaceNameInternal))
            .addToRepository(fluentFactory.newOperationInterface()
                .withName(interfaceNameExternalRequired))
            .addToRepository(fluentFactory.newOperationInterface()
                .withName(interfaceNameExternalProvided))
            .addToRepository(fluentFactory.newBasicComponent()
                .withName(componentNameOne)
                .provides(fluentFactory.fetchOfOperationInterface(interfaceNameInternal), roleNameInternalProvided)
                .requires(fluentFactory.fetchOfOperationInterface(interfaceNameExternalRequired),
                        roleNameExternalRequiredInner))
            .addToRepository(fluentFactory.newBasicComponent()
                .withName(componentNameTwo)
                .requires(fluentFactory.fetchOfOperationInterface(interfaceNameInternal), roleNameInternalRequired)
                .provides(fluentFactory.fetchOfOperationInterface(interfaceNameExternalProvided),
                        roleNameExternalProvidedInner))
            .addToRepository(fluentFactory.newCompositeComponent()
                .provides(fluentFactory.fetchOfOperationInterface(interfaceNameExternalProvided),
                        roleNameExternalProvidedOuter)
                .requires(fluentFactory.fetchOfOperationInterface(interfaceNameExternalRequired),
                        roleNameExternalRequiredOuter)
                .withAssemblyContext(fluentFactory.fetchOfComponent(componentNameOne), contextNameOne)
                .withAssemblyContext(fluentFactory.fetchOfComponent(componentNameTwo), contextNameTwo)
                .withAssemblyConnection(fluentFactory.fetchOfOperationProvidedRole(roleNameInternalProvided),
                        fluentFactory.fetchOfAssemblyContext(contextNameOne),
                        fluentFactory.fetchOfOperationRequiredRole(roleNameInternalRequired),
                        fluentFactory.fetchOfAssemblyContext(contextNameTwo))
                .withProvidedDelegationConnection(fluentFactory.fetchOfAssemblyContext(contextNameTwo),
                        fluentFactory.fetchOfOperationProvidedRole(roleNameExternalProvidedInner),
                        fluentFactory.fetchOfOperationProvidedRole(roleNameExternalProvidedOuter))
                .withRequiredDelegationConnection(fluentFactory.fetchOfAssemblyContext(contextNameOne),
                        fluentFactory.fetchOfOperationRequiredRole(roleNameExternalRequiredInner),
                        fluentFactory.fetchOfOperationRequiredRole(roleNameExternalRequiredOuter)));

        // Fill blackboard
        blackboard.addPartition(BLACKBOARD_INPUT_REPOSITORY, fluentRepository.createRepositoryNow());

        // Create and run job
        final MoCoReJob job = new MoCoReJob(blackboard, BLACKBOARD_INPUT_REPOSITORY, BLACKBOARD_OUTPUT_REPOSITORY,
                BLACKBOARD_OUTPUT_SYSTEM, BLACKBOARD_OUTPUT_ALLOCATION, BLACKBOARD_OUTPUT_RESOURCEENVIRONMENT);
        job.execute(new NullProgressMonitor());

        // Check if components exist in repository
        final Repository outputRepository = (Repository) blackboard.getPartition(BLACKBOARD_OUTPUT_REPOSITORY);
        final EList<RepositoryComponent> components = outputRepository.getComponents__Repository();
        final CompositeComponent composite = (CompositeComponent) components.stream()
            .filter(component -> component instanceof CompositeComponent)
            .findFirst()
            .orElseThrow();
        assertEquals(2, composite.getAssemblyContexts__ComposedStructure()
            .size());

        // Check if assembly connector created correctly
        final List<AssemblyConnector> assemblyConnectors = composite.getConnectors__ComposedStructure()
            .stream()
            .filter(genericConnector -> genericConnector instanceof AssemblyConnector)
            .map(genericConnector -> (AssemblyConnector) genericConnector)
            .collect(Collectors.toList());
        assertEquals(1, assemblyConnectors.size());
        final AssemblyConnector assemblyConnector = assemblyConnectors.get(0);
        assertEquals(componentNameOne, assemblyConnector.getProvidingAssemblyContext_AssemblyConnector()
            .getEncapsulatedComponent__AssemblyContext()
            .getEntityName());
        assertEquals(componentNameTwo, assemblyConnector.getRequiringAssemblyContext_AssemblyConnector()
            .getEncapsulatedComponent__AssemblyContext()
            .getEntityName());
        assertEquals(interfaceNameInternal, assemblyConnector.getProvidedRole_AssemblyConnector()
            .getProvidedInterface__OperationProvidedRole()
            .getEntityName());
        assertEquals(interfaceNameInternal, assemblyConnector.getRequiredRole_AssemblyConnector()
            .getRequiredInterface__OperationRequiredRole()
            .getEntityName());

        // Check if provided delegation created correctly
        final List<ProvidedDelegationConnector> providedDelegations = composite.getConnectors__ComposedStructure()
            .stream()
            .filter(genericConnector -> genericConnector instanceof ProvidedDelegationConnector)
            .map(genericConnector -> (ProvidedDelegationConnector) genericConnector)
            .collect(Collectors.toList());
        assertEquals(1, providedDelegations.size());
        final ProvidedDelegationConnector providedDelegationConnector = providedDelegations.get(0);
        assertEquals(componentNameTwo, providedDelegationConnector.getAssemblyContext_ProvidedDelegationConnector()
            .getEncapsulatedComponent__AssemblyContext()
            .getEntityName());
        assertEquals(interfaceNameExternalProvided,
                providedDelegationConnector.getInnerProvidedRole_ProvidedDelegationConnector()
                    .getProvidedInterface__OperationProvidedRole()
                    .getEntityName());
        assertEquals(interfaceNameExternalProvided,
                providedDelegationConnector.getOuterProvidedRole_ProvidedDelegationConnector()
                    .getProvidedInterface__OperationProvidedRole()
                    .getEntityName());

        // Check if required delegation created correctly
        final List<RequiredDelegationConnector> requiredDelegations = composite.getConnectors__ComposedStructure()
            .stream()
            .filter(genericConnector -> genericConnector instanceof RequiredDelegationConnector)
            .map(genericConnector -> (RequiredDelegationConnector) genericConnector)
            .collect(Collectors.toList());
        assertEquals(1, requiredDelegations.size());
        final RequiredDelegationConnector requiredDelegationConnector = requiredDelegations.get(0);
        assertEquals(componentNameOne, requiredDelegationConnector.getAssemblyContext_RequiredDelegationConnector()
            .getEncapsulatedComponent__AssemblyContext()
            .getEntityName());
        assertEquals(interfaceNameExternalRequired,
                requiredDelegationConnector.getInnerRequiredRole_RequiredDelegationConnector()
                    .getRequiredInterface__OperationRequiredRole()
                    .getEntityName());
        assertEquals(interfaceNameExternalRequired,
                requiredDelegationConnector.getOuterRequiredRole_RequiredDelegationConnector()
                    .getRequiredInterface__OperationRequiredRole()
                    .getEntityName());
    }

    @Test
    public void testRecursiveProvisionLeadsToSystemDelegation() throws Exception {
        // Create blackboard and fluent repository
        final Blackboard<Object> blackboard = new Blackboard<>();
        final FluentRepositoryFactory fluentFactory = new FluentRepositoryFactory();
        final Repo fluentRepository = fluentFactory.newRepository();

        // Create composite component and add to fluent repository
        fluentRepository.addToRepository(fluentFactory.newOperationInterface()
            .withName("Doable"))
            .addToRepository(fluentFactory.newBasicComponent()
                .withName("Child")
                .provides(fluentFactory.fetchOfOperationInterface("Doable"), "Doable Role"))
            .addToRepository(fluentFactory.newCompositeComponent()
                .withName("Inner Parent")
                .withAssemblyContext(fluentFactory.fetchOfComponent("Child")))
            .addToRepository(fluentFactory.newCompositeComponent()
                .withName("Middle Parent")
                .withAssemblyContext(fluentFactory.fetchOfComponent("Inner Parent")))
            .addToRepository(fluentFactory.newCompositeComponent()
                .withName("Outer Parent")
                .withAssemblyContext(fluentFactory.fetchOfComponent("Middle Parent")));

        // Fill blackboard
        blackboard.addPartition(BLACKBOARD_INPUT_REPOSITORY, fluentRepository.createRepositoryNow());

        // Create and run job
        final MoCoReJob job = new MoCoReJob(blackboard, BLACKBOARD_INPUT_REPOSITORY, BLACKBOARD_OUTPUT_REPOSITORY,
                BLACKBOARD_OUTPUT_SYSTEM, BLACKBOARD_OUTPUT_ALLOCATION, BLACKBOARD_OUTPUT_RESOURCEENVIRONMENT);
        job.execute(new NullProgressMonitor());

        // Check inner provision was recursively
        final Repository outputRepository = (Repository) blackboard.getPartition(BLACKBOARD_OUTPUT_REPOSITORY);
        final EList<RepositoryComponent> components = outputRepository.getComponents__Repository();
        final List<CompositeComponent> composites = components.stream()
            .filter(component -> component instanceof CompositeComponent)
            .map(component -> (CompositeComponent) component)
            .toList();
        composites.forEach(composite -> assertTrue(composite.getProvidedRoles_InterfaceProvidingEntity()
            .stream()
            .anyMatch(role -> role instanceof OperationProvidedRole
                    && ((OperationProvidedRole) role).getProvidedInterface__OperationProvidedRole()
                        .getEntityName()
                        .equals("Doable"))));

        // Check most outer provision was delegated by system
        final System outputSystem = (System) blackboard.getPartition(BLACKBOARD_OUTPUT_SYSTEM);
        final EList<Connector> connectors = outputSystem.getConnectors__ComposedStructure();
        assertEquals(1, connectors.size());
        final ProvidedDelegationConnector delegationConnector = (ProvidedDelegationConnector) connectors.get(0);
        assertEquals("Outer Parent", delegationConnector.getAssemblyContext_ProvidedDelegationConnector()
            .getEncapsulatedComponent__AssemblyContext()
            .getEntityName());
        assertEquals("Doable", delegationConnector.getInnerProvidedRole_ProvidedDelegationConnector()
            .getProvidedInterface__OperationProvidedRole()
            .getEntityName());
        assertEquals("Doable", delegationConnector.getOuterProvidedRole_ProvidedDelegationConnector()
            .getProvidedInterface__OperationProvidedRole()
            .getEntityName());
    }
}
