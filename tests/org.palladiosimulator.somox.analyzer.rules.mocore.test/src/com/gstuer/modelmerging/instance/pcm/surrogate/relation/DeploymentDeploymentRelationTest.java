package com.gstuer.modelmerging.instance.pcm.surrogate.relation;

import com.gstuer.modelmerging.framework.surrogate.RelationTest;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Deployment;

public class DeploymentDeploymentRelationTest
        extends RelationTest<DeploymentDeploymentRelation, Deployment, Deployment> {
    @Override
    protected DeploymentDeploymentRelation createRelation(Deployment source, Deployment destination,
            boolean isPlaceholder) {
        return new DeploymentDeploymentRelation(source, destination, isPlaceholder);
    }

    @Override
    protected Deployment getUniqueSourceEntity() {
        return Deployment.getUniquePlaceholder();
    }

    @Override
    protected Deployment getUniqueDestinationEntity() {
        return Deployment.getUniquePlaceholder();
    }
}
