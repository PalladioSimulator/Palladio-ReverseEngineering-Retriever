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
    public ComponentAssemblyRelationProcessor(final PcmSurrogate model) {
        super(model, ComponentAssemblyRelation.class);
    }

    @Override
    protected void refine(final ComponentAssemblyRelation discovery) {
        // Identify all allocations of the providing and consuming component in the assembly
        final Component<?> provider = discovery.getSource()
            .getSource();
        final Component<?> consumer = discovery.getDestination()
            .getSource();
        final Interface providerConsumerInterface = discovery.getSource()
            .getDestination();
        final List<Deployment> providerAllocations = this.getAllocatedContainers(provider);
        final List<Deployment> consumerAllocations = this.getAllocatedContainers(consumer);

        // Add link between allocation containers of assembled components if needed
        if (providerAllocations.isEmpty()) {
            final Deployment placeholderDeployment = Deployment.getUniquePlaceholder();
            final ComponentAllocationRelation allocation = new ComponentAllocationRelation(provider,
                    placeholderDeployment, true);
            providerAllocations.add(placeholderDeployment);
            this.addImplication(allocation);
        }
        if (consumerAllocations.isEmpty()) {
            final Deployment placeholderDeployment = Deployment.getUniquePlaceholder();
            final ComponentAllocationRelation allocation = new ComponentAllocationRelation(consumer,
                    placeholderDeployment, true);
            consumerAllocations.add(placeholderDeployment);
            this.addImplication(allocation);
        }
        for (final Deployment providerContainer : providerAllocations) {
            for (final Deployment consumerContainer : consumerAllocations) {
                if (!providerContainer.equals(consumerContainer)) {
                    // Connect every providing container with each consuming one, except they are
                    // the same container
                    final DeploymentDeploymentRelation containerLink = new DeploymentDeploymentRelation(
                            providerContainer, consumerContainer, true);
                    this.addImplication(containerLink);
                }
            }
        }

        // Remove component assembly fully-placeholder relation (non-direct & non-indirect)
        final List<ComponentAssemblyRelation> assemblies = this.getModel()
            .getByType(ComponentAssemblyRelation.class);
        assemblies.removeIf(assembly -> !assembly.getSource()
            .isPlaceholder()
                || !assembly.getDestination()
                    .isPlaceholder());
        for (final ComponentAssemblyRelation placeholderAssembly : assemblies) {
            if (discovery.equals(placeholderAssembly)) {
                continue;
            }
            final Component<?> source = placeholderAssembly.getSource()
                .getSource();
            final Component<?> destination = placeholderAssembly.getDestination()
                .getSource();
            final Interface sourceDestinationInterface = placeholderAssembly.getSource()
                .getDestination();
            // Placeholder are unique and can only be allocated to a single container
            final Optional<Deployment> optionalSourceContainer = this.getAllocatedContainers(source)
                .stream()
                .findFirst();
            final Optional<Deployment> optionalDestinationContainer = this.getAllocatedContainers(destination)
                .stream()
                .findFirst();

            if (optionalSourceContainer.isPresent() && optionalDestinationContainer.isPresent()) {
                final Deployment sourceContainer = optionalSourceContainer.get();
                final Deployment destinationContainer = optionalDestinationContainer.get();

                // Container links are bi-directional => Parallel or inverse assemblies are valid
                final boolean isParallelAssembly = providerAllocations.contains(sourceContainer)
                        && consumerAllocations.contains(destinationContainer);
                final boolean isInverseAssembly = providerAllocations.contains(destinationContainer)
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

    private List<Deployment> getAllocatedContainers(final Component<?> component) {
        final List<ComponentAllocationRelation> allocations = this.getModel()
            .getByType(ComponentAllocationRelation.class);
        return allocations.stream()
            .filter(allocation -> allocation.getSource()
                .equals(component))
            .map(ComponentAllocationRelation::getDestination)
            .collect(Collectors.toList());
    }
}
