package org.palladiosimulator.retriever.mocore.surrogate.relation;

import org.palladiosimulator.retriever.mocore.surrogate.element.Interface;
import org.palladiosimulator.retriever.mocore.surrogate.element.Signature;

import tools.mdsd.mocore.framework.surrogate.Relation;
import tools.mdsd.mocore.framework.surrogate.Replaceable;

public class SignatureProvisionRelation extends Relation<Signature, Interface> {
    public SignatureProvisionRelation(final Signature source, final Interface destination,
            final boolean isPlaceholder) {
        super(source, destination, isPlaceholder);
    }

    @Override
    public <U extends Replaceable> SignatureProvisionRelation replace(final U original, final U replacement) {
        if (!this.includes(original)) {
            // TODO Add message to exception
            throw new IllegalArgumentException();
        }
        if (this.equals(original)) {
            return (SignatureProvisionRelation) replacement;
        }
        final Signature source = this.getSourceReplacement(original, replacement);
        final Interface destination = this.getDestinationReplacement(original, replacement);
        return new SignatureProvisionRelation(source, destination, this.isPlaceholder());
    }
}
