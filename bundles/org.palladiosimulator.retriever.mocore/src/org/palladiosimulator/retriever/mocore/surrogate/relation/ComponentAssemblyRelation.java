package org.palladiosimulator.retriever.mocore.surrogate.relation;

import java.util.Objects;

import tools.mdsd.mocore.framework.surrogate.Relation;
import tools.mdsd.mocore.framework.surrogate.Replaceable;

public class ComponentAssemblyRelation extends Relation<InterfaceProvisionRelation, InterfaceRequirementRelation> {
    private static final String ERROR_UNEQUAL_INTERFACE = "Interfaces of relations have to be equal.";

    public ComponentAssemblyRelation(InterfaceProvisionRelation source, InterfaceRequirementRelation destination,
            boolean isPlaceholder) {
        super(source, destination, isPlaceholder);
        if (!Objects.equals(source.getDestination(), destination.getDestination())) {
            throw new IllegalArgumentException(ERROR_UNEQUAL_INTERFACE);
        }
    }

    @Override
    public <U extends Replaceable> ComponentAssemblyRelation replace(U original, U replacement) {
        if (!this.includes(original)) {
            // TODO Add message to exception
            throw new IllegalArgumentException();
        }
        if (this.equals(original)) {
            return (ComponentAssemblyRelation) replacement;
        }
        InterfaceProvisionRelation source = getSourceReplacement(original, replacement);
        InterfaceRequirementRelation destination = getDestinationReplacement(original, replacement);
        return new ComponentAssemblyRelation(source, destination, this.isPlaceholder());
    }
}
