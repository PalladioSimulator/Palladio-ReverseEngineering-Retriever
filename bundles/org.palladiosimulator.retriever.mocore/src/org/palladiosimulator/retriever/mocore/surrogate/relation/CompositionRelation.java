package org.palladiosimulator.retriever.mocore.surrogate.relation;

import org.palladiosimulator.retriever.mocore.surrogate.element.Component;
import org.palladiosimulator.retriever.mocore.surrogate.element.Composite;

import tools.mdsd.mocore.framework.surrogate.Relation;
import tools.mdsd.mocore.framework.surrogate.Replaceable;

public class CompositionRelation extends Relation<Composite, Component<?>> {
    public CompositionRelation(final Composite source, final Component<?> destination, final boolean isPlaceholder) {
        super(source, destination, isPlaceholder);
    }

    @Override
    public <T extends Replaceable> CompositionRelation replace(final T original, final T replacement) {
        if (!this.includes(original)) {
            // TODO Add message to exception
            throw new IllegalArgumentException();
        }
        if (this.equals(original)) {
            return (CompositionRelation) replacement;
        }
        final Composite source = this.getSourceReplacement(original, replacement);
        final Component<?> destination = this.getDestinationReplacement(original, replacement);
        return new CompositionRelation(source, destination, this.isPlaceholder());
    }
}
