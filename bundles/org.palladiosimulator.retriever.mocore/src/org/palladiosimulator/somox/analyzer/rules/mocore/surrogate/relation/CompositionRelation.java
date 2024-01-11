package org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Component;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Composite;

import tools.mdsd.mocore.framework.surrogate.Relation;
import tools.mdsd.mocore.framework.surrogate.Replaceable;

public class CompositionRelation extends Relation<Composite, Component<?>> {
    public CompositionRelation(Composite source, Component<?> destination, boolean isPlaceholder) {
        super(source, destination, isPlaceholder);
    }

    @Override
    public <T extends Replaceable> CompositionRelation replace(T original, T replacement) {
        if (!this.includes(original)) {
            // TODO Add message to exception
            throw new IllegalArgumentException();
        }
        if (this.equals(original)) {
            return (CompositionRelation) replacement;
        }
        Composite source = getSourceReplacement(original, replacement);
        Component<?> destination = getDestinationReplacement(original, replacement);
        return new CompositionRelation(source, destination, this.isPlaceholder());
    }
}
