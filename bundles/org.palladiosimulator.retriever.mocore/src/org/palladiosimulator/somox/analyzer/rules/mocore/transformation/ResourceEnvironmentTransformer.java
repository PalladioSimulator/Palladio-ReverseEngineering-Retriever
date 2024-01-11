package org.palladiosimulator.somox.analyzer.rules.mocore.transformation;

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
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Deployment;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.LinkResourceSpecification;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.LinkResourceSpecificationRelation;

import com.google.common.collect.HashMultimap;
import tools.mdsd.mocore.framework.transformation.Transformer;

public class ResourceEnvironmentTransformer implements Transformer<PcmSurrogate, ResourceEnvironment> {
    @Override
    public ResourceEnvironment transform(PcmSurrogate model) {
        FluentResourceEnvironmentFactory resourceEnvironmentFactory = new FluentResourceEnvironmentFactory();
        IResourceEnvironment fluentResourceEnvironment = resourceEnvironmentFactory.newResourceEnvironment();

        // Add resource containers to resource environment
        for (Deployment deployment : model.getByType(Deployment.class)) {
            ResourceContainerCreator containerCreator = getContainerCreator(resourceEnvironmentFactory, deployment);
            fluentResourceEnvironment.addToResourceEnvironment(containerCreator);
        }

        HashMultimap<LinkResourceSpecification, Deployment> linkSpecificationMap = HashMultimap.create();
        for (LinkResourceSpecificationRelation linkingRelation : model
                .getByType(LinkResourceSpecificationRelation.class)) {
            Deployment source = linkingRelation.getDestination().getSource();
            Deployment destination = linkingRelation.getDestination().getDestination();
            LinkResourceSpecification specification = linkingRelation.getSource();

            // The if clause filters non-wrong but trivial A->A container links
            if (!source.equals(destination)) {
                linkSpecificationMap.put(specification, source);
                linkSpecificationMap.put(specification, destination);
            }
        }

        // Add linking resources (specification <-> [deployment <-> deployment]) to resource environment
        for (LinkResourceSpecification key : linkSpecificationMap.keySet()) {
            LinkingResourceCreator linkingResourceCreator = getLinkingResourceCreator(resourceEnvironmentFactory,
                    linkSpecificationMap.get(key));
            fluentResourceEnvironment.addToResourceEnvironment(linkingResourceCreator);
        }

        // Create PCM resource environment
        ResourceEnvironment resourceEnvironment = fluentResourceEnvironment.createResourceEnvironmentNow();

        // Copy resource specifications from old to new containers
        for (ResourceContainer container : resourceEnvironment.getResourceContainer_ResourceEnvironment()) {
            for (Deployment deployment : model.getByType(Deployment.class)) {
                // TODO Use container wrapper.equals
                ResourceContainer wrappedContainer = deployment.getValue();
                if (container.getEntityName().equals(wrappedContainer.getEntityName())) {
                    container.getActiveResourceSpecifications_ResourceContainer()
                            .addAll(wrappedContainer.getActiveResourceSpecifications_ResourceContainer());
                    container.getHddResourceSpecifications().addAll(wrappedContainer.getHddResourceSpecifications());
                }
            }
        }

        // Add linking resource specifications to PCM linking resources
        for (LinkResourceSpecification specification : linkSpecificationMap.keySet()) {
            Set<Deployment> deployments = linkSpecificationMap.get(specification);
            for (LinkingResource linkingResource : resourceEnvironment.getLinkingResources__ResourceEnvironment()) {
                if (Objects.equals(getLinkingResourceName(deployments), linkingResource.getEntityName())) {
                    linkingResource
                            .setCommunicationLinkResourceSpecifications_LinkingResource(specification.getValue());
                }
            }
        }

        return resourceEnvironment;
    }

    protected static String getLinkingResourceName(Collection<Deployment> deployments) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Deployment deployment : deployments) {
            stringBuilder.append(" " + deployment.getValue().getEntityName());
        }
        stringBuilder.append(" Link");
        return stringBuilder.toString();
    }

    private ResourceContainerCreator getContainerCreator(FluentResourceEnvironmentFactory fluentFactory,
            Deployment deployment) {
        ResourceContainer wrappedContainer = deployment.getValue();

        // Create a container creator instance w/o processing specifications due to missing fluentApi copy support
        ResourceContainerCreator containerCreator = fluentFactory.newResourceContainer()
                .withName(wrappedContainer.getEntityName());
        return containerCreator;
    }

    private LinkingResourceCreator getLinkingResourceCreator(FluentResourceEnvironmentFactory fluentFactory,
            Collection<Deployment> deployments) {
        // Create a linking resource creator w/o specifications due to missing fluentApi copy support
        String entityName = getLinkingResourceName(deployments);
        LinkingResourceCreator creator = fluentFactory.newLinkingResource()
                .withName(entityName);
        for (Deployment deployment : deployments) {
            String containerName = deployment.getValue().getEntityName();
            creator.addLinkedResourceContainer(containerName);
        }
        return creator;
    }
}
