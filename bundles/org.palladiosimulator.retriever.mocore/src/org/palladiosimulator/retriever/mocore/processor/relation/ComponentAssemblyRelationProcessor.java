package org.palladiosimulator.retriever.mocore.processor.relation;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.Component;
import org.palladiosimulator.retriever.mocore.surrogate.element.Deployment;
import org.palladiosimulator.retriever.mocore.surrogate.element.Interface;
import org.palladiosimulator.retriever.mocore.surrogate.relation.ComponentAllocationRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.ComponentAssemblyRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.DeploymentDeploymentRelation;

import tools.mdsd.mocore.framework.processor.RelationProcessor;

public class ComponentAssemblyRelationProcessor extends RelationProcessor<PcmSurrogate, ComponentAssemblyRelation> {
    public ComponentAssemblyRelationProcessor(PcmSurrogate model) {
        super(model, ComponentAssemblyRelation.class);
    }

    @Override
    protected void refine(ComponentAssemblyRelation discovery) {
        // Identify all allocations of the providing and consuming component in the assembly
        Component<?> provider = discovery.getSource()
            .getSource();
        Component<?> consumer = discovery.getDestination()
            .getSource();
        Interface providerConsumerInterface = discovery.getSource()
            .getDestination();
        List<Deployment> providerAllocations = getAllocatedContainers(provider);
        List<Deployment> consumerAllocations = getAllocatedContainers(consumer);

        // Add link between allocation containers of assembled components if needed
        if (providerAllocations.isEmpty()) {
            Deployment placeholderDeployment = Deployment.getUniquePlaceholder();
            ComponentAllocationRelation allocation = new ComponentAllocationRelation(provider, placeholderDeployment,
                    true);
            providerAllocations.add(placeholderDeployment);
            this.addImplication(allocation);
        }
        if (consumerAllocations.isEmpty()) {
            Deployment placeholderDeployment = Deployment.getUniquePlaceholder();
            ComponentAllocationRelation allocation = new ComponentAllocationRelation(consumer, placeholderDeployment,
                    true);
            consumerAllocations.add(placeholderDeployment);
            this.addImplication(allocation);
        }
        for (Deployment providerContainer : providerAllocations) {
            for (Deployment consumerContainer : consumerAllocations) {
                if (!providerContainer.equals(consumerContainer)) {
                    // Connect every providing container with each consuming one, except they are
                    // the same container
                    DeploymentDeploymentRelation containerLink = new DeploymentDeploymentRelation(providerContainer,
                            consumerContainer, true);
                    this.addImplication(containerLink);
                }
            }
        }

        // Remove component assembly fully-placeholder relation (non-direct & non-indirect)
        List<ComponentAssemblyRelation> assemblies = this.getModel()
            .getByType(ComponentAssemblyRelation.class);
        assemblies.removeIf(assembly -> !assembly.getSource()
            .isPlaceholder()
                || !assembly.getDestination()
                    .isPlaceholder());
        for (ComponentAssemblyRelation placeholderAssembly : assemblies) {
            if (discovery.equals(placeholderAssembly)) {
                continue;
            }
            Component<?> source = placeholderAssembly.getSource()
                .getSource();
            Component<?> destination = placeholderAssembly.getDestination()
                .getSource();
            Interface sourceDestinationInterface = placeholderAssembly.getSource()
                .getDestination();
            // Placeholder are unique and can only be allocated to a single container
            Optional<Deployment> optionalSourceContainer = getAllocatedContainers(source).stream()
                .findFirst();
            Optional<Deployment> optionalDestinationContainer = getAllocatedContainers(destination).stream()
                .findFirst();

            if (optionalSourceContainer.isPresent() && optionalDestinationContainer.isPresent()) {
                Deployment sourceContainer = optionalSourceContainer.get();
                Deployment destinationContainer = optionalDestinationContainer.get();

                // Container links are bi-directional => Parallel or inverse assemblies are valid
                boolean isParallelAssembly = providerAllocations.contains(sourceContainer)
                        && consumerAllocations.contains(destinationContainer);
                boolean isInverseAssembly = providerAllocations.contains(destinationContainer)
                        && consumerAllocations.contains(sourceContainer);
                if (isParallelAssembly || isInverseAssembly) {
                    this.addImplications(this.getModel()
                        .replace(placeholderAssembly, discovery));
                    this.addImplications(this.getModel()
                        .replace(source, provider));
                    this.addImplications(this.getModel()
                        .replace(destination, consumer));
                    this.addImplications(this.getModel()
                        .replace(sourceDestinationInterface, providerConsumerInterface));
                }
            }
        }

        super.refine(discovery);
    }

    private List<Deployment> getAllocatedContainers(Component<?> component) {
        List<ComponentAllocationRelation> allocations = this.getModel()
            .getByType(ComponentAllocationRelation.class);
        return allocations.stream()
            .filter(allocation -> allocation.getSource()
                .equals(component))
            .map(ComponentAllocationRelation::getDestination)
            .collect(Collectors.toList());
    }
}
