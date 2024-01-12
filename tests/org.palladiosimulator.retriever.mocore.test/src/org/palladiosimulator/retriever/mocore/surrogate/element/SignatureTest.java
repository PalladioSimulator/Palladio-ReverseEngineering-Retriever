package org.palladiosimulator.retriever.mocore.surrogate.element;

import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.RepositoryFactory;

import tools.mdsd.mocore.framework.surrogate.ElementTest;
import tools.mdsd.mocore.utility.IdentifierGenerator;

public class SignatureTest extends ElementTest<Signature, OperationSignature> {
    @Override
    protected Signature createElement(final OperationSignature value, final boolean isPlaceholder) {
        return new Signature(value, isPlaceholder);
    }

    @Override
    protected OperationSignature getUniqueValue() {
        final String identifier = IdentifierGenerator.getUniqueIdentifier();
        final OperationSignature value = RepositoryFactory.eINSTANCE.createOperationSignature();
        value.setEntityName(identifier);
        return value;
    }

    @Override
    protected Signature getUniqueNonPlaceholder() {
        return new Signature(this.getUniqueValue(), false);
    }

    @Override
    protected Signature getPlaceholderOf(final Signature replaceable) {
        return new Signature(replaceable.getValue(), true);
    }
}
