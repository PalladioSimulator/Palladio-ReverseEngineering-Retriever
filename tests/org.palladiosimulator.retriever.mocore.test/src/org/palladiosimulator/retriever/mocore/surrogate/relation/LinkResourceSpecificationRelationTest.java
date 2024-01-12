package org.palladiosimulator.retriever.mocore.surrogate.relation;

import org.palladiosimulator.retriever.mocore.surrogate.element.Deployment;
import org.palladiosimulator.retriever.mocore.surrogate.element.LinkResourceSpecification;

import tools.mdsd.mocore.framework.surrogate.RelationTest;

public class LinkResourceSpecificationRelationTest extends
        RelationTest<LinkResourceSpecificationRelation, LinkResourceSpecification, DeploymentDeploymentRelation> {
    @Override
    protected LinkResourceSpecificationRelation createRelation(final LinkResourceSpecification source,
            final DeploymentDeploymentRelation destination, final boolean isPlaceholder) {
        return new LinkResourceSpecificationRelation(source, destination, isPlaceholder);
    }

    @Override
    protected LinkResourceSpecification getUniqueSourceEntity() {
        return LinkResourceSpecification.getUniquePlaceholder();
    }

    @Override
    protected DeploymentDeploymentRelation getUniqueDestinationEntity() {
        return new DeploymentDeploymentRelation(Deployment.getUniquePlaceholder(), Deployment.getUniquePlaceholder(),
                true);
    }
}
