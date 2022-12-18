package org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Component;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Deployment;

import com.gstuer.modelmerging.framework.surrogate.Relation;
import com.gstuer.modelmerging.framework.surrogate.Replaceable;

public class ComponentAllocationRelation extends Relation<Component, Deployment> {
    public ComponentAllocationRelation(Component source, Deployment destination, boolean isPlaceholder) {
        super(source, destination, isPlaceholder);
    }

    @Override
    public <U extends Replaceable> ComponentAllocationRelation replace(U original, U replacement) {
        if (!this.includes(original)) {
            // TODO Add message to exception
            throw new IllegalArgumentException();
        }
        if (this.equals(original)) {
            return (ComponentAllocationRelation) replacement;
        }
        Component source = getSourceReplacement(original, replacement);
        Deployment destination = getDestinationReplacement(original, replacement);
        return new ComponentAllocationRelation(source, destination, this.isPlaceholder());
    }
}
