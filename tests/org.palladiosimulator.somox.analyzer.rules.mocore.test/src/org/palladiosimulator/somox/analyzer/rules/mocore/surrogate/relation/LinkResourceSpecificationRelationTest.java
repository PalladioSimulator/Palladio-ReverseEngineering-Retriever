package org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Deployment;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.LinkResourceSpecification;

import com.gstuer.modelmerging.framework.surrogate.RelationTest;

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
