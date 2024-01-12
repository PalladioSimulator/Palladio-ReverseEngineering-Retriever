package org.palladiosimulator.retriever.mocore.surrogate.relation;

import java.util.Objects;

import tools.mdsd.mocore.framework.surrogate.Relation;
import tools.mdsd.mocore.framework.surrogate.Replaceable;

public class ComponentSignatureProvisionRelation
        extends Relation<InterfaceProvisionRelation, SignatureProvisionRelation> {
    private static final String ERROR_UNEQUAL_INTERFACE = "Interfaces of relations have to be equal.";

    public ComponentSignatureProvisionRelation(InterfaceProvisionRelation source,
            SignatureProvisionRelation destination, boolean isPlaceholder) {
        super(source, destination, isPlaceholder);
        if (!Objects.equals(source.getDestination(), destination.getDestination())) {
            throw new IllegalArgumentException(ERROR_UNEQUAL_INTERFACE);
        }
    }

    @Override
    public <U extends Replaceable> ComponentSignatureProvisionRelation replace(U original, U replacement) {
        if (!this.includes(original)) {
            // TODO Add message to exception
            throw new IllegalArgumentException();
        }
        if (this.equals(original)) {
            return (ComponentSignatureProvisionRelation) replacement;
        }
        InterfaceProvisionRelation source = getSourceReplacement(original, replacement);
        SignatureProvisionRelation destination = getDestinationReplacement(original, replacement);
        return new ComponentSignatureProvisionRelation(source, destination, this.isPlaceholder());
    }
}
