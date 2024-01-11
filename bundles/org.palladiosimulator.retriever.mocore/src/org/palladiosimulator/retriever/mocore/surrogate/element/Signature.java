package org.palladiosimulator.retriever.mocore.surrogate.element;

import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.RepositoryFactory;

public class Signature extends PcmElement<OperationSignature> {
    public Signature(OperationSignature value, boolean isPlaceholder) {
        super(value, isPlaceholder);
    }

    public static Signature getUniquePlaceholder() {
        String identifier = "Placeholder_" + getUniqueValue();
        OperationSignature value = RepositoryFactory.eINSTANCE.createOperationSignature();
        value.setEntityName(identifier);
        return new Signature(value, true);
    }
}
