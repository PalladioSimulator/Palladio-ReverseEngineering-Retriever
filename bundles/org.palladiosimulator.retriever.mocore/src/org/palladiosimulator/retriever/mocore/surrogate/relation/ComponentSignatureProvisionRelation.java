package org.palladiosimulator.retriever.mocore.surrogate.relation;

import java.util.Objects;

import tools.mdsd.mocore.framework.surrogate.Relation;
import tools.mdsd.mocore.framework.surrogate.Replaceable;

public class ComponentSignatureProvisionRelation
        extends Relation<InterfaceProvisionRelation, SignatureProvisionRelation> {
    private static final String ERROR_UNEQUAL_INTERFACE = "Interfaces of relations have to be equal.";

    public ComponentSignatureProvisionRelation(final InterfaceProvisionRelation source,
            final SignatureProvisionRelation destination, final boolean isPlaceholder) {
        super(source, destination, isPlaceholder);
        if (!Objects.equals(source.getDestination(), destination.getDestination())) {
            throw new IllegalArgumentException(ERROR_UNEQUAL_INTERFACE);
        }
    }

    @Override
    public <U extends Replaceable> ComponentSignatureProvisionRelation replace(final U original, final U replacement) {
        if (!this.includes(original)) {
            // TODO Add message to exception
            throw new IllegalArgumentException();
        }
        if (this.equals(original)) {
            return (ComponentSignatureProvisionRelation) replacement;
        }
        final InterfaceProvisionRelation source = this.getSourceReplacement(original, replacement);
        final SignatureProvisionRelation destination = this.getDestinationReplacement(original, replacement);
        return new ComponentSignatureProvisionRelation(source, destination, this.isPlaceholder());
    }
}
