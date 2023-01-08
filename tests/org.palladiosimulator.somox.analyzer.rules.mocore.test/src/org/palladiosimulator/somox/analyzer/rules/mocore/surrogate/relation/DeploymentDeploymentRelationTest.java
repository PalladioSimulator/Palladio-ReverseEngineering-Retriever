package org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Deployment;

import tools.mdsd.mocore.framework.surrogate.RelationTest;

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
