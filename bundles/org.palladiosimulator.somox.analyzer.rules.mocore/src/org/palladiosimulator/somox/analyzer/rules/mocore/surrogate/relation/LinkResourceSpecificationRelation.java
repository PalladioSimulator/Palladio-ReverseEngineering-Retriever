package org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.LinkResourceSpecification;

import tools.mdsd.mocore.framework.surrogate.Relation;
import tools.mdsd.mocore.framework.surrogate.Replaceable;

public class LinkResourceSpecificationRelation
        extends Relation<LinkResourceSpecification, DeploymentDeploymentRelation> {
    public LinkResourceSpecificationRelation(LinkResourceSpecification source, DeploymentDeploymentRelation destination,
            boolean isPlaceholder) {
        super(source, destination, isPlaceholder);
    }

    @Override
    public <U extends Replaceable> LinkResourceSpecificationRelation replace(U original, U replacement) {
        if (!this.includes(original)) {
            // TODO Add message to exception
            throw new IllegalArgumentException();
        }
        if (this.equals(original)) {
            return (LinkResourceSpecificationRelation) replacement;
        }
        LinkResourceSpecification source = getSourceReplacement(original, replacement);
        DeploymentDeploymentRelation destination = getDestinationReplacement(original, replacement);
        return new LinkResourceSpecificationRelation(source, destination, this.isPlaceholder());
    }
}
