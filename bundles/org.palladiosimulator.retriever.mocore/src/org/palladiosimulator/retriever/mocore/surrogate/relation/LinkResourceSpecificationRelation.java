package org.palladiosimulator.retriever.mocore.surrogate.relation;

import org.palladiosimulator.retriever.mocore.surrogate.element.LinkResourceSpecification;

import tools.mdsd.mocore.framework.surrogate.Relation;
import tools.mdsd.mocore.framework.surrogate.Replaceable;

public class LinkResourceSpecificationRelation
        extends Relation<LinkResourceSpecification, DeploymentDeploymentRelation> {
    public LinkResourceSpecificationRelation(final LinkResourceSpecification source,
            final DeploymentDeploymentRelation destination, final boolean isPlaceholder) {
        super(source, destination, isPlaceholder);
    }

    @Override
    public <U extends Replaceable> LinkResourceSpecificationRelation replace(final U original, final U replacement) {
        if (!this.includes(original)) {
            // TODO Add message to exception
            throw new IllegalArgumentException();
        }
        if (this.equals(original)) {
            return (LinkResourceSpecificationRelation) replacement;
        }
        final LinkResourceSpecification source = this.getSourceReplacement(original, replacement);
        final DeploymentDeploymentRelation destination = this.getDestinationReplacement(original, replacement);
        return new LinkResourceSpecificationRelation(source, destination, this.isPlaceholder());
    }
}
