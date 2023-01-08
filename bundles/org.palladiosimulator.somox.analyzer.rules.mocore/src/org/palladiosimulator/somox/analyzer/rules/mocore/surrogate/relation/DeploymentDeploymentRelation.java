package org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Deployment;

import tools.mdsd.mocore.framework.surrogate.Relation;
import tools.mdsd.mocore.framework.surrogate.Replaceable;

public class DeploymentDeploymentRelation extends Relation<Deployment, Deployment> {
    public DeploymentDeploymentRelation(Deployment source, Deployment destination, boolean isPlaceholder) {
        super(source, destination, isPlaceholder);
    }

    @Override
    public <U extends Replaceable> DeploymentDeploymentRelation replace(U original, U replacement) {
        if (!this.includes(original)) {
            // TODO Add message to exception
            throw new IllegalArgumentException();
        }
        if (this.equals(original)) {
            return (DeploymentDeploymentRelation) replacement;
        }
        Deployment source = getSourceReplacement(original, replacement);
        Deployment destination = getDestinationReplacement(original, replacement);
        return new DeploymentDeploymentRelation(source, destination, this.isPlaceholder());
    }
}
