package org.palladiosimulator.retriever.mocore.processor.relation;

import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.Deployment;
import org.palladiosimulator.retriever.mocore.surrogate.element.LinkResourceSpecification;
import org.palladiosimulator.retriever.mocore.surrogate.relation.DeploymentDeploymentRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.LinkResourceSpecificationRelation;
import org.palladiosimulator.retriever.mocore.utility.ElementFactory;

import tools.mdsd.mocore.framework.processor.RelationProcessorTest;

public class LinkResourceSpecificationRelationProcessorTest extends
        RelationProcessorTest<LinkResourceSpecificationRelationProcessor, PcmSurrogate, LinkResourceSpecificationRelation, LinkResourceSpecification, DeploymentDeploymentRelation> {
    @Override
    protected LinkResourceSpecificationRelation createRelation(LinkResourceSpecification source,
            DeploymentDeploymentRelation destination, boolean isPlaceholder) {
        return new LinkResourceSpecificationRelation(source, destination, isPlaceholder);
    }

    @Override
    protected LinkResourceSpecification getUniqueNonPlaceholderSourceEntity() {
        return ElementFactory.createUniqueLinkResourceSpecification(false);
    }

    @Override
    protected LinkResourceSpecification getPlaceholderOfSourceEntity(LinkResourceSpecification source) {
        return new LinkResourceSpecification(source.getValue(), true);
    }

    @Override
    protected DeploymentDeploymentRelation getUniqueNonPlaceholderDestinationEntity() {
        return new DeploymentDeploymentRelation(Deployment.getUniquePlaceholder(), Deployment.getUniquePlaceholder(),
                false);
    }

    @Override
    protected DeploymentDeploymentRelation getPlaceholderOfDestinationEntity(DeploymentDeploymentRelation destination) {
        return new DeploymentDeploymentRelation(destination.getSource(), destination.getDestination(), true);
    }

    @Override
    protected LinkResourceSpecificationRelationProcessor createProcessor(PcmSurrogate model) {
        return new LinkResourceSpecificationRelationProcessor(model);
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }
}
