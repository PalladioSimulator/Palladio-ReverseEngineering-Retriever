package org.palladiosimulator.somox.analyzer.rules.mocore.workflow;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.junit.jupiter.api.Test;
import org.palladiosimulator.generator.fluent.repository.api.Repo;
import org.palladiosimulator.generator.fluent.repository.factory.FluentRepositoryFactory;
import org.palladiosimulator.pcm.core.composition.AssemblyConnector;
import org.palladiosimulator.pcm.repository.CompositeComponent;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryComponent;

import de.uka.ipd.sdq.workflow.blackboard.Blackboard;

public class MoCoReJobTest {
    private final static String BLACKBOARD_OUTPUT_REPOSITORY = "input.repository";
    private final static String BLACKBOARD_INPUT_REPOSITORY = "output.repository";
    private final static String BLACKBOARD_OUTPUT_SYSTEM = "output.system";
    private final static String BLACKBOARD_OUTPUT_ALLOCATION = "output.allocation";
    private final static String BLACKBOARD_OUTPUT_RESOURCEENVIRONMENT = "output.resource";

    @Test
    public void testConstructorWithValidInput() {
        Blackboard<Object> blackboard = new Blackboard<Object>();
        assertDoesNotThrow(() -> new MoCoReJob(blackboard, BLACKBOARD_INPUT_REPOSITORY,
                BLACKBOARD_OUTPUT_REPOSITORY, BLACKBOARD_OUTPUT_SYSTEM, BLACKBOARD_OUTPUT_ALLOCATION,
                BLACKBOARD_OUTPUT_RESOURCEENVIRONMENT));
    }

    @Test
    public void testCompositeComponentProcessing() throws Exception {
        // Tests constants
        String componentNameOne = "Component One";
        String contextNameOne = componentNameOne + " Context";
        String componentNameTwo = "Component Two";
        String contextNameTwo = componentNameTwo + " Context";
        String interfaceNameInternal = "Internal Interface";
        String roleNameInternalRequired = "Role Requirer " + interfaceNameInternal;
        String roleNameInternalProvided = "Role Provider " + interfaceNameInternal;
        String interfaceNameExternalRequired = "External Interface Required";
        String interfaceNameExternalProvided = "External Interface Provided";

        // Create blackboard and fluent repository
        Blackboard<Object> blackboard = new Blackboard<Object>();
        FluentRepositoryFactory fluentFactory = new FluentRepositoryFactory();
        Repo fluentRepository = fluentFactory.newRepository();

        // Create composite component and add to fluent repository
        fluentRepository
                .addToRepository(fluentFactory.newOperationInterface().withName(interfaceNameInternal))
                .addToRepository(fluentFactory.newOperationInterface().withName(interfaceNameExternalRequired))
                .addToRepository(fluentFactory.newOperationInterface().withName(interfaceNameExternalProvided))
                .addToRepository(fluentFactory.newBasicComponent()
                        .withName(componentNameOne)
                        .provides(fluentFactory.fetchOfOperationInterface(interfaceNameInternal),
                                roleNameInternalProvided))
                .addToRepository(fluentFactory.newBasicComponent()
                        .withName(componentNameTwo)
                        .requires(fluentFactory.fetchOfOperationInterface(interfaceNameInternal),
                                roleNameInternalRequired))
                .addToRepository(fluentFactory.newCompositeComponent()
                        .withAssemblyContext(fluentFactory.fetchOfComponent(componentNameOne), contextNameOne)
                        .withAssemblyContext(fluentFactory.fetchOfComponent(componentNameTwo), contextNameTwo)
                        .withAssemblyConnection(
                                fluentFactory.fetchOfOperationProvidedRole(roleNameInternalProvided),
                                fluentFactory.fetchOfAssemblyContext(contextNameOne),
                                fluentFactory.fetchOfOperationRequiredRole(roleNameInternalRequired),
                                fluentFactory.fetchOfAssemblyContext(contextNameTwo)));

        // Fill blackboard
        blackboard.addPartition(BLACKBOARD_INPUT_REPOSITORY, fluentRepository.createRepositoryNow());

        // Create and run job
        MoCoReJob job = new MoCoReJob(blackboard, BLACKBOARD_INPUT_REPOSITORY,
                BLACKBOARD_OUTPUT_REPOSITORY, BLACKBOARD_OUTPUT_SYSTEM, BLACKBOARD_OUTPUT_ALLOCATION,
                BLACKBOARD_OUTPUT_RESOURCEENVIRONMENT);
        job.execute(new NullProgressMonitor());

        // Check if components exist in repository
        Repository outputRepository = (Repository) blackboard.getPartition(BLACKBOARD_OUTPUT_REPOSITORY);
        EList<RepositoryComponent> components = outputRepository.getComponents__Repository();
        assertEquals(3, components.size());
        CompositeComponent composite = (CompositeComponent) components.stream()
                .filter(component -> component instanceof CompositeComponent).findFirst().orElseThrow();
        assertEquals(2, composite.getAssemblyContexts__ComposedStructure().size());

        // Check if assembly connector created correctly
        assertEquals(1, composite.getConnectors__ComposedStructure().size());
        AssemblyConnector connector = (AssemblyConnector) composite.getConnectors__ComposedStructure().get(0);
        assertEquals(componentNameOne, connector.getProvidingAssemblyContext_AssemblyConnector()
                .getEncapsulatedComponent__AssemblyContext().getEntityName());
        assertEquals(componentNameTwo, connector.getRequiringAssemblyContext_AssemblyConnector()
                .getEncapsulatedComponent__AssemblyContext().getEntityName());
        assertEquals(interfaceNameInternal, connector.getProvidedRole_AssemblyConnector()
                .getProvidedInterface__OperationProvidedRole().getEntityName());
        assertEquals(interfaceNameInternal, connector.getRequiredRole_AssemblyConnector()
                .getRequiredInterface__OperationRequiredRole().getEntityName());

        // TODO Check if delegations created correctly
    }
}
