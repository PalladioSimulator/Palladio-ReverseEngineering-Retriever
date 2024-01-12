package org.palladiosimulator.retriever.mocore.surrogate.element;

import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.RepositoryFactory;

public class Signature extends PcmElement<OperationSignature> {
    public Signature(final OperationSignature value, final boolean isPlaceholder) {
        super(value, isPlaceholder);
    }

    public static Signature getUniquePlaceholder() {
        final String identifier = "Placeholder_" + getUniqueValue();
        final OperationSignature value = RepositoryFactory.eINSTANCE.createOperationSignature();
        value.setEntityName(identifier);
        return new Signature(value, true);
    }
}
