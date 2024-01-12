package org.palladiosimulator.retriever.mocore.surrogate.relation;

import java.util.Objects;

import tools.mdsd.mocore.framework.surrogate.Relation;
import tools.mdsd.mocore.framework.surrogate.Replaceable;

public class ComponentAssemblyRelation extends Relation<InterfaceProvisionRelation, InterfaceRequirementRelation> {
    private static final String ERROR_UNEQUAL_INTERFACE = "Interfaces of relations have to be equal.";

    public ComponentAssemblyRelation(final InterfaceProvisionRelation source,
            final InterfaceRequirementRelation destination, final boolean isPlaceholder) {
        super(source, destination, isPlaceholder);
        if (!Objects.equals(source.getDestination(), destination.getDestination())) {
            throw new IllegalArgumentException(ERROR_UNEQUAL_INTERFACE);
        }
    }

    @Override
    public <U extends Replaceable> ComponentAssemblyRelation replace(final U original, final U replacement) {
        if (!this.includes(original)) {
            // TODO Add message to exception
            throw new IllegalArgumentException();
        }
        if (this.equals(original)) {
            return (ComponentAssemblyRelation) replacement;
        }
        final InterfaceProvisionRelation source = this.getSourceReplacement(original, replacement);
        final InterfaceRequirementRelation destination = this.getDestinationReplacement(original, replacement);
        return new ComponentAssemblyRelation(source, destination, this.isPlaceholder());
    }
}
