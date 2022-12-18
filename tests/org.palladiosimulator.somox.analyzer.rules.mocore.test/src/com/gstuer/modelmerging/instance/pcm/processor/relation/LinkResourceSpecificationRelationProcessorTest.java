package com.gstuer.modelmerging.instance.pcm.processor.relation;

import com.gstuer.modelmerging.framework.processor.RelationProcessorTest;
import com.gstuer.modelmerging.instance.pcm.surrogate.PcmSurrogate;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Deployment;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.LinkResourceSpecification;
import com.gstuer.modelmerging.instance.pcm.surrogate.relation.DeploymentDeploymentRelation;
import com.gstuer.modelmerging.instance.pcm.surrogate.relation.LinkResourceSpecificationRelation;
import com.gstuer.modelmerging.instance.pcm.utility.ElementFactory;

public class LinkResourceSpecificationRelationProcessorTest
        extends RelationProcessorTest<LinkResourceSpecificationRelationProcessor, PcmSurrogate,
                LinkResourceSpecificationRelation, LinkResourceSpecification, DeploymentDeploymentRelation> {
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
        return new DeploymentDeploymentRelation(Deployment.getUniquePlaceholder(),
                Deployment.getUniquePlaceholder(), false);
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
