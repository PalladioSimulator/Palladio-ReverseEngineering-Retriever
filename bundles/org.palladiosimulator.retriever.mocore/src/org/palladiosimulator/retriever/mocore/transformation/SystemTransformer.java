package org.palladiosimulator.retriever.mocore.transformation;

import org.palladiosimulator.generator.fluent.system.api.ISystemAddition;
import org.palladiosimulator.generator.fluent.system.factory.FluentSystemFactory;
import org.palladiosimulator.generator.fluent.system.structure.AssemblyContextCreator;
import org.palladiosimulator.generator.fluent.system.structure.connector.operation.AssemblyConnectorCreator;
import org.palladiosimulator.generator.fluent.system.structure.connector.operation.ProvidedDelegationConnectorCreator;
import org.palladiosimulator.generator.fluent.system.structure.role.OperationProvidedRoleCreator;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.Component;
import org.palladiosimulator.retriever.mocore.surrogate.element.Interface;
import org.palladiosimulator.retriever.mocore.surrogate.relation.ComponentAssemblyRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.CompositionRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.InterfaceProvisionRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.InterfaceRequirementRelation;

import tools.mdsd.mocore.framework.transformation.Transformer;

public class SystemTransformer implements Transformer<PcmSurrogate, org.palladiosimulator.pcm.system.System> {
    private static final String ASSEMBLY_CONTEXT_NAME_PATTERN = "%s Assembly Context";
    private static final String ASSEMBLY_CONNECTOR_NAME_PATTERN = "%s Connector";
    private static final String DELEGATION_ROLE_NAME_PATTERN = "%s Delegation-Provider";
    private static final String DELEGATION_CONNECTOR_NAME_PATTERN = "%s Delegation Connector";

    @Override
    public System transform(PcmSurrogate model) {
        Repository repository = new RepositoryTransformer().transform(model);
        return this.transform(model, repository);
    }

    public System transform(PcmSurrogate model, Repository repository) {
        FluentSystemFactory systemFactory = new FluentSystemFactory();
        ISystemAddition fluentSystem = systemFactory.newSystem()
            .addRepository(repository);

        // Add repository components as assembly contexts to system
        for (Component<?> component : model.getByType(Component.class)) {
            AssemblyContextCreator contextCreator = getAssemblyContextCreator(systemFactory, component);
            fluentSystem.addToSystem(contextCreator);
        }

        // Add assembly connectors (component assembly relations)
        for (ComponentAssemblyRelation relation : model.getByType(ComponentAssemblyRelation.class)) {
            AssemblyConnectorCreator connectorCreator = getAssemblyConnectorCreator(systemFactory, relation);
            fluentSystem.addToSystem(connectorCreator);
        }

        // Add provided delegation connectors for provided non-required interfaces
        for (InterfaceProvisionRelation relation : model.getByType(InterfaceProvisionRelation.class)) {
            Interface providedInteface = relation.getDestination();
            String providedIntefaceName = providedInteface.getValue()
                .getEntityName();
            Component<?> provider = relation.getSource();
            boolean existsRequirement = model.getByType(InterfaceRequirementRelation.class)
                .stream()
                .anyMatch(requirementRelation -> requirementRelation.getDestination()
                    .equals(providedInteface));
            boolean isCompositeChild = model.getByType(CompositionRelation.class)
                .stream()
                .anyMatch(composition -> composition.getDestination()
                    .equals(provider));
            // Check whether interface should be excluded from delegation
            boolean excludeDelegation = RepositoryTransformer.isExcludedFromDelegation(provider, providedInteface);

            // Only add delegation if no other component requires interface and only add for most
            // outer provider in case
            // of composite structures
            //
            // Important: Asserts that repository transformer added provision delegation from
            // innermost to outermost
            // component in case of a composite structure. If not, no delegation to system is added.
            if (!existsRequirement && !isCompositeChild && !excludeDelegation) {
                // Create & add provided role to fluent system
                String delegationRoleName = String.format(DELEGATION_ROLE_NAME_PATTERN, providedIntefaceName);
                OperationProvidedRoleCreator systemProvidedRole = systemFactory.newOperationProvidedRole()
                    .withName(delegationRoleName)
                    .withProvidedInterface(providedIntefaceName);
                fluentSystem.addToSystem(systemProvidedRole);

                // Create & add delegation between context provided role & system provided role
                String delegationConnectorName = String.format(DELEGATION_CONNECTOR_NAME_PATTERN, providedIntefaceName);
                ProvidedDelegationConnectorCreator systemDelegation = systemFactory
                    .newProvidedDelegationConnectorCreator()
                    .withName(delegationConnectorName)
                    .withOuterProvidedRole(delegationRoleName)
                    .withProvidingContext(getAssemblyContextName(provider))
                    .withOperationProvidedRole(RepositoryTransformer.getProvidedRoleName(providedInteface));
                fluentSystem.addToSystem(systemDelegation);
            }
        }

        return fluentSystem.createSystemNow();
    }

    protected static String getAssemblyContextName(Component<?> component) {
        String componentEntityName = component.getValue()
            .getEntityName();
        return String.format(ASSEMBLY_CONTEXT_NAME_PATTERN, componentEntityName);
    }

    protected static String getAssemblyConnectorName(Interface interfaceInstance) {
        String interfaceEntityName = interfaceInstance.getValue()
            .getEntityName();
        return String.format(ASSEMBLY_CONNECTOR_NAME_PATTERN, interfaceEntityName);
    }

    private AssemblyContextCreator getAssemblyContextCreator(FluentSystemFactory fluentFactory,
            Component<?> component) {
        String componentEntityName = component.getValue()
            .getEntityName();
        String assemblyContextName = getAssemblyContextName(component);
        AssemblyContextCreator contextCreator = fluentFactory.newAssemblyContext()
            .withName(assemblyContextName)
            .withEncapsulatedComponent(componentEntityName);
        return contextCreator;
    }

    private AssemblyConnectorCreator getAssemblyConnectorCreator(FluentSystemFactory fluentFactory,
            ComponentAssemblyRelation assemblyRelation) {
        // Get wrapper from relation
        Component<?> provider = assemblyRelation.getSource()
            .getSource();
        Component<?> consumer = assemblyRelation.getDestination()
            .getSource();
        Interface interfaceInstance = assemblyRelation.getSource()
            .getDestination();

        // Get entity names of roles, components and connector
        String connectorName = getAssemblyConnectorName(interfaceInstance);
        String providerName = getAssemblyContextName(provider);
        String consumerName = getAssemblyContextName(consumer);
        String providedRoleName = RepositoryTransformer.getProvidedRoleName(interfaceInstance);
        String requiredRoleName = RepositoryTransformer.getRequiredRoleName(interfaceInstance);

        AssemblyConnectorCreator connectorCreator = fluentFactory.newAssemblyConnector()
            .withName(connectorName)
            .withProvidingAssemblyContext(providerName)
            .withOperationProvidedRole(providedRoleName)
            .withRequiringAssemblyContext(consumerName)
            .withOperationRequiredRole(requiredRoleName);
        return connectorCreator;
    }
}
