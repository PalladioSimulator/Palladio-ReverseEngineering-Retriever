package org.palladiosimulator.retriever.mocore.transformation;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import org.palladiosimulator.generator.fluent.resourceenvironment.api.IResourceEnvironment;
import org.palladiosimulator.generator.fluent.resourceenvironment.factory.FluentResourceEnvironmentFactory;
import org.palladiosimulator.generator.fluent.resourceenvironment.structure.LinkingResourceCreator;
import org.palladiosimulator.generator.fluent.resourceenvironment.structure.ResourceContainerCreator;
import org.palladiosimulator.pcm.resourceenvironment.LinkingResource;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.Deployment;
import org.palladiosimulator.retriever.mocore.surrogate.element.LinkResourceSpecification;
import org.palladiosimulator.retriever.mocore.surrogate.relation.LinkResourceSpecificationRelation;

import com.google.common.collect.HashMultimap;

import tools.mdsd.mocore.framework.transformation.Transformer;

public class ResourceEnvironmentTransformer implements Transformer<PcmSurrogate, ResourceEnvironment> {
    @Override
    public ResourceEnvironment transform(final PcmSurrogate model) {
        final FluentResourceEnvironmentFactory resourceEnvironmentFactory = new FluentResourceEnvironmentFactory();
        final IResourceEnvironment fluentResourceEnvironment = resourceEnvironmentFactory.newResourceEnvironment();

        // Add resource containers to resource environment
        for (final Deployment deployment : model.getByType(Deployment.class)) {
            final ResourceContainerCreator containerCreator = this.getContainerCreator(resourceEnvironmentFactory,
                    deployment);
            fluentResourceEnvironment.addToResourceEnvironment(containerCreator);
        }

        final HashMultimap<LinkResourceSpecification, Deployment> linkSpecificationMap = HashMultimap.create();
        for (final LinkResourceSpecificationRelation linkingRelation : model
            .getByType(LinkResourceSpecificationRelation.class)) {
            final Deployment source = linkingRelation.getDestination()
                .getSource();
            final Deployment destination = linkingRelation.getDestination()
                .getDestination();
            final LinkResourceSpecification specification = linkingRelation.getSource();

            // The if clause filters non-wrong but trivial A->A container links
            if (!source.equals(destination)) {
                linkSpecificationMap.put(specification, source);
                linkSpecificationMap.put(specification, destination);
            }
        }

        // Add linking resources (specification <-> [deployment <-> deployment]) to resource
        // environment
        for (final LinkResourceSpecification key : linkSpecificationMap.keySet()) {
            final LinkingResourceCreator linkingResourceCreator = this
                .getLinkingResourceCreator(resourceEnvironmentFactory, linkSpecificationMap.get(key));
            fluentResourceEnvironment.addToResourceEnvironment(linkingResourceCreator);
        }

        // Create PCM resource environment
        final ResourceEnvironment resourceEnvironment = fluentResourceEnvironment.createResourceEnvironmentNow();

        // Copy resource specifications from old to new containers
        for (final ResourceContainer container : resourceEnvironment.getResourceContainer_ResourceEnvironment()) {
            for (final Deployment deployment : model.getByType(Deployment.class)) {
                // TODO Use container wrapper.equals
                final ResourceContainer wrappedContainer = deployment.getValue();
                if (container.getEntityName()
                    .equals(wrappedContainer.getEntityName())) {
                    container.getActiveResourceSpecifications_ResourceContainer()
                        .addAll(wrappedContainer.getActiveResourceSpecifications_ResourceContainer());
                    container.getHddResourceSpecifications()
                        .addAll(wrappedContainer.getHddResourceSpecifications());
                }
            }
        }

        // Add linking resource specifications to PCM linking resources
        for (final LinkResourceSpecification specification : linkSpecificationMap.keySet()) {
            final Set<Deployment> deployments = linkSpecificationMap.get(specification);
            for (final LinkingResource linkingResource : resourceEnvironment
                .getLinkingResources__ResourceEnvironment()) {
                if (Objects.equals(getLinkingResourceName(deployments), linkingResource.getEntityName())) {
                    linkingResource
                        .setCommunicationLinkResourceSpecifications_LinkingResource(specification.getValue());
                }
            }
        }

        return resourceEnvironment;
    }

    protected static String getLinkingResourceName(final Collection<Deployment> deployments) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (final Deployment deployment : deployments) {
            stringBuilder.append(" " + deployment.getValue()
                .getEntityName());
        }
        stringBuilder.append(" Link");
        return stringBuilder.toString();
    }

    private ResourceContainerCreator getContainerCreator(final FluentResourceEnvironmentFactory fluentFactory,
            final Deployment deployment) {
        final ResourceContainer wrappedContainer = deployment.getValue();

        // Create a container creator instance w/o processing specifications due to missing
        // fluentApi copy support
        final ResourceContainerCreator containerCreator = fluentFactory.newResourceContainer()
            .withName(wrappedContainer.getEntityName());
        return containerCreator;
    }

    private LinkingResourceCreator getLinkingResourceCreator(final FluentResourceEnvironmentFactory fluentFactory,
            final Collection<Deployment> deployments) {
        // Create a linking resource creator w/o specifications due to missing fluentApi copy
        // support
        final String entityName = getLinkingResourceName(deployments);
        final LinkingResourceCreator creator = fluentFactory.newLinkingResource()
            .withName(entityName);
        for (final Deployment deployment : deployments) {
            final String containerName = deployment.getValue()
                .getEntityName();
            creator.addLinkedResourceContainer(containerName);
        }
        return creator;
    }
}
