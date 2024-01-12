package org.palladiosimulator.retriever.mocore.surrogate.relation;

import org.palladiosimulator.retriever.mocore.surrogate.element.Component;
import org.palladiosimulator.retriever.mocore.surrogate.element.Interface;

import tools.mdsd.mocore.framework.surrogate.Relation;
import tools.mdsd.mocore.framework.surrogate.Replaceable;

public class InterfaceRequirementRelation extends Relation<Component<?>, Interface> {
    public InterfaceRequirementRelation(final Component<?> source, final Interface destination,
            final boolean isPlaceholder) {
        super(source, destination, isPlaceholder);
    }

    @Override
    public <U extends Replaceable> InterfaceRequirementRelation replace(final U original, final U replacement) {
        if (!this.includes(original)) {
            // TODO Add message to exception
            throw new IllegalArgumentException();
        }
        if (this.equals(original)) {
            return (InterfaceRequirementRelation) replacement;
        }
        final Component<?> source = this.getSourceReplacement(original, replacement);
        final Interface destination = this.getDestinationReplacement(original, replacement);
        return new InterfaceRequirementRelation(source, destination, this.isPlaceholder());
    }
}
