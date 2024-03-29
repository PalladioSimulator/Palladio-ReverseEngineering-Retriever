package org.palladiosimulator.retriever.mocore.surrogate.relation;

import org.palladiosimulator.retriever.mocore.surrogate.element.Deployment;

import tools.mdsd.mocore.framework.surrogate.RelationTest;

public class DeploymentDeploymentRelationTest
        extends RelationTest<DeploymentDeploymentRelation, Deployment, Deployment> {
    @Override
    protected DeploymentDeploymentRelation createRelation(final Deployment source, final Deployment destination,
            final boolean isPlaceholder) {
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
