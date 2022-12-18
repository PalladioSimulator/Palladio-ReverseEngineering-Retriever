package com.gstuer.modelmerging.instance.pcm.surrogate.relation;

import com.gstuer.modelmerging.framework.surrogate.RelationTest;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Deployment;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.LinkResourceSpecification;

public class LinkResourceSpecificationRelationTest extends
        RelationTest<LinkResourceSpecificationRelation, LinkResourceSpecification, DeploymentDeploymentRelation> {
    @Override
    protected LinkResourceSpecificationRelation createRelation(LinkResourceSpecification source,
            DeploymentDeploymentRelation destination, boolean isPlaceholder) {
        return new LinkResourceSpecificationRelation(source, destination, isPlaceholder);
    }

    @Override
    protected LinkResourceSpecification getUniqueSourceEntity() {
        return LinkResourceSpecification.getUniquePlaceholder();
    }

    @Override
    protected DeploymentDeploymentRelation getUniqueDestinationEntity() {
        return new DeploymentDeploymentRelation(Deployment.getUniquePlaceholder(),
                Deployment.getUniquePlaceholder(), true);
    }
}
