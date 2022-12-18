package com.gstuer.modelmerging.instance.pcm.transformation;

import org.palladiosimulator.generator.fluent.system.api.ISystemAddition;
import org.palladiosimulator.generator.fluent.system.factory.FluentSystemFactory;
import org.palladiosimulator.generator.fluent.system.structure.AssemblyContextCreator;
import org.palladiosimulator.generator.fluent.system.structure.connector.operation.AssemblyConnectorCreator;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.system.System;

import com.gstuer.modelmerging.framework.transformation.Transformer;
import com.gstuer.modelmerging.instance.pcm.surrogate.PcmSurrogate;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Component;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Interface;
import com.gstuer.modelmerging.instance.pcm.surrogate.relation.ComponentAssemblyRelation;

public class SystemTransformer implements Transformer<PcmSurrogate, org.palladiosimulator.pcm.system.System> {
    private static final String ASSEMBLY_CONTEXT_NAME_PATTERN = "%s Assembly Context";
    private static final String ASSEMBLY_CONNECTOR_NAME_PATTERN = "%s Connector";

    @Override
    public System transform(PcmSurrogate model) {
        FluentSystemFactory systemFactory = new FluentSystemFactory();
        Repository repository = new RepositoryTransformer().transform(model);
        ISystemAddition fluentSystem = systemFactory.newSystem().addRepository(repository);

        // Add repository components as assembly contexts to system
        for (Component component : model.getByType(Component.class)) {
            AssemblyContextCreator contextCreator = getAssemblyContextCreator(systemFactory, component);
            fluentSystem.addToSystem(contextCreator);
        }

        // Add assembly connectors (component assembly relations)
        for (ComponentAssemblyRelation relation : model.getByType(ComponentAssemblyRelation.class)) {
            AssemblyConnectorCreator connectorCreator = getAssemblyConnectorCreator(systemFactory, relation);
            fluentSystem.addToSystem(connectorCreator);
        }

        return fluentSystem.createSystemNow();
    }

    protected static String getAssemblyContextName(Component component) {
        String componentEntityName = component.getValue().getEntityName();
        return String.format(ASSEMBLY_CONTEXT_NAME_PATTERN, componentEntityName);
    }

    protected static String getAssemblyConnectorName(Interface interfaceInstance) {
        String interfaceEntityName = interfaceInstance.getValue().getEntityName();
        return String.format(ASSEMBLY_CONNECTOR_NAME_PATTERN, interfaceEntityName);
    }

    private AssemblyContextCreator getAssemblyContextCreator(FluentSystemFactory fluentFactory, Component component) {
        String componentEntityName = component.getValue().getEntityName();
        String assemblyContextName = getAssemblyContextName(component);
        AssemblyContextCreator contextCreator = fluentFactory.newAssemblyContext()
                .withName(assemblyContextName)
                .withEncapsulatedComponent(componentEntityName);
        return contextCreator;
    }

    private AssemblyConnectorCreator getAssemblyConnectorCreator(FluentSystemFactory fluentFactory,
            ComponentAssemblyRelation assemblyRelation) {
        // Get wrapper from relation
        Component provider = assemblyRelation.getSource().getSource();
        Component consumer = assemblyRelation.getDestination().getSource();
        Interface interfaceInstance = assemblyRelation.getSource().getDestination();

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
