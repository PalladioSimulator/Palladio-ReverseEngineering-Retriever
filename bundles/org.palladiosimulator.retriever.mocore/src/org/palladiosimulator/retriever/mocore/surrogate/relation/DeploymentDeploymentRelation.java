package org.palladiosimulator.retriever.mocore.surrogate.relation;

import org.palladiosimulator.retriever.mocore.surrogate.element.Deployment;

import tools.mdsd.mocore.framework.surrogate.Relation;
import tools.mdsd.mocore.framework.surrogate.Replaceable;

public class DeploymentDeploymentRelation extends Relation<Deployment, Deployment> {
    public DeploymentDeploymentRelation(final Deployment source, final Deployment destination,
            final boolean isPlaceholder) {
        super(source, destination, isPlaceholder);
    }

    @Override
    public <U extends Replaceable> DeploymentDeploymentRelation replace(final U original, final U replacement) {
        if (!this.includes(original)) {
            // TODO Add message to exception
            throw new IllegalArgumentException();
        }
        if (this.equals(original)) {
            return (DeploymentDeploymentRelation) replacement;
        }
        final Deployment source = this.getSourceReplacement(original, replacement);
        final Deployment destination = this.getDestinationReplacement(original, replacement);
        return new DeploymentDeploymentRelation(source, destination, this.isPlaceholder());
    }
}
