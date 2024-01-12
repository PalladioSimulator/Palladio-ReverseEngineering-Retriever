package org.palladiosimulator.retriever.mocore.surrogate.relation;

import org.palladiosimulator.retriever.mocore.surrogate.element.Deployment;
import org.palladiosimulator.retriever.mocore.surrogate.element.LinkResourceSpecification;
import org.palladiosimulator.retriever.mocore.surrogate.relation.DeploymentDeploymentRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.LinkResourceSpecificationRelation;

import tools.mdsd.mocore.framework.surrogate.RelationTest;

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
        return new DeploymentDeploymentRelation(Deployment.getUniquePlaceholder(), Deployment.getUniquePlaceholder(),
                true);
    }
}
