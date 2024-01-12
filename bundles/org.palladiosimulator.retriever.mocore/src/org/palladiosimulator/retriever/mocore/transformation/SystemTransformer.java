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
    public System transform(final PcmSurrogate model) {
        final Repository repository = new RepositoryTransformer().transform(model);
        return this.transform(model, repository);
    }

    public System transform(final PcmSurrogate model, final Repository repository) {
        final FluentSystemFactory systemFactory = new FluentSystemFactory();
        final ISystemAddition fluentSystem = systemFactory.newSystem()
            .addRepository(repository);

        // Add repository components as assembly contexts to system
        for (final Component<?> component : model.getByType(Component.class)) {
            final AssemblyContextCreator contextCreator = this.getAssemblyContextCreator(systemFactory, component);
            fluentSystem.addToSystem(contextCreator);
        }

        // Add assembly connectors (component assembly relations)
        for (final ComponentAssemblyRelation relation : model.getByType(ComponentAssemblyRelation.class)) {
            final AssemblyConnectorCreator connectorCreator = this.getAssemblyConnectorCreator(systemFactory, relation);
            fluentSystem.addToSystem(connectorCreator);
        }

        // Add provided delegation connectors for provided non-required interfaces
        for (final InterfaceProvisionRelation relation : model.getByType(InterfaceProvisionRelation.class)) {
            final Interface providedInteface = relation.getDestination();
            final String providedIntefaceName = providedInteface.getValue()
                .getEntityName();
            final Component<?> provider = relation.getSource();
            final boolean existsRequirement = model.getByType(InterfaceRequirementRelation.class)
                .stream()
                .anyMatch(requirementRelation -> requirementRelation.getDestination()
                    .equals(providedInteface));
            final boolean isCompositeChild = model.getByType(CompositionRelation.class)
                .stream()
                .anyMatch(composition -> composition.getDestination()
                    .equals(provider));
            // Check whether interface should be excluded from delegation
            final boolean excludeDelegation = RepositoryTransformer.isExcludedFromDelegation(provider,
                    providedInteface);

            // Only add delegation if no other component requires interface and only add for most
            // outer provider in case
            // of composite structures
            //
            // Important: Asserts that repository transformer added provision delegation from
            // innermost to outermost
            // component in case of a composite structure. If not, no delegation to system is added.
            if (!existsRequirement && !isCompositeChild && !excludeDelegation) {
                // Create & add provided role to fluent system
                final String delegationRoleName = String.format(DELEGATION_ROLE_NAME_PATTERN, providedIntefaceName);
                final OperationProvidedRoleCreator systemProvidedRole = systemFactory.newOperationProvidedRole()
                    .withName(delegationRoleName)
                    .withProvidedInterface(providedIntefaceName);
                fluentSystem.addToSystem(systemProvidedRole);

                // Create & add delegation between context provided role & system provided role
                final String delegationConnectorName = String.format(DELEGATION_CONNECTOR_NAME_PATTERN,
                        providedIntefaceName);
                final ProvidedDelegationConnectorCreator systemDelegation = systemFactory
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

    protected static String getAssemblyContextName(final Component<?> component) {
        final String componentEntityName = component.getValue()
            .getEntityName();
        return String.format(ASSEMBLY_CONTEXT_NAME_PATTERN, componentEntityName);
    }

    protected static String getAssemblyConnectorName(final Interface interfaceInstance) {
        final String interfaceEntityName = interfaceInstance.getValue()
            .getEntityName();
        return String.format(ASSEMBLY_CONNECTOR_NAME_PATTERN, interfaceEntityName);
    }

    private AssemblyContextCreator getAssemblyContextCreator(final FluentSystemFactory fluentFactory,
            final Component<?> component) {
        final String componentEntityName = component.getValue()
            .getEntityName();
        final String assemblyContextName = getAssemblyContextName(component);
        final AssemblyContextCreator contextCreator = fluentFactory.newAssemblyContext()
            .withName(assemblyContextName)
            .withEncapsulatedComponent(componentEntityName);
        return contextCreator;
    }

    private AssemblyConnectorCreator getAssemblyConnectorCreator(final FluentSystemFactory fluentFactory,
            final ComponentAssemblyRelation assemblyRelation) {
        // Get wrapper from relation
        final Component<?> provider = assemblyRelation.getSource()
            .getSource();
        final Component<?> consumer = assemblyRelation.getDestination()
            .getSource();
        final Interface interfaceInstance = assemblyRelation.getSource()
            .getDestination();

        // Get entity names of roles, components and connector
        final String connectorName = getAssemblyConnectorName(interfaceInstance);
        final String providerName = getAssemblyContextName(provider);
        final String consumerName = getAssemblyContextName(consumer);
        final String providedRoleName = RepositoryTransformer.getProvidedRoleName(interfaceInstance);
        final String requiredRoleName = RepositoryTransformer.getRequiredRoleName(interfaceInstance);

        final AssemblyConnectorCreator connectorCreator = fluentFactory.newAssemblyConnector()
            .withName(connectorName)
            .withProvidingAssemblyContext(providerName)
            .withOperationProvidedRole(providedRoleName)
            .withRequiringAssemblyContext(consumerName)
            .withOperationRequiredRole(requiredRoleName);
        return connectorCreator;
    }
}
