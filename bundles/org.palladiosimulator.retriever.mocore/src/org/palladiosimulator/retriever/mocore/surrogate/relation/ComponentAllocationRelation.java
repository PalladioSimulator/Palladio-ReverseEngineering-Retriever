package org.palladiosimulator.retriever.mocore.surrogate.relation;

import org.palladiosimulator.retriever.mocore.surrogate.element.Component;
import org.palladiosimulator.retriever.mocore.surrogate.element.Deployment;

import tools.mdsd.mocore.framework.surrogate.Relation;
import tools.mdsd.mocore.framework.surrogate.Replaceable;

public class ComponentAllocationRelation extends Relation<Component<?>, Deployment> {
    public ComponentAllocationRelation(final Component<?> source, final Deployment destination,
            final boolean isPlaceholder) {
        super(source, destination, isPlaceholder);
    }

    @Override
    public <U extends Replaceable> ComponentAllocationRelation replace(final U original, final U replacement) {
        if (!this.includes(original)) {
            // TODO Add message to exception
            throw new IllegalArgumentException();
        }
        if (this.equals(original)) {
            return (ComponentAllocationRelation) replacement;
        }
        final Component<?> source = this.getSourceReplacement(original, replacement);
        final Deployment destination = this.getDestinationReplacement(original, replacement);
        return new ComponentAllocationRelation(source, destination, this.isPlaceholder());
    }
}
