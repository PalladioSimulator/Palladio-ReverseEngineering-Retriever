package org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element;

import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.RepositoryFactory;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Signature;

import com.gstuer.modelmerging.framework.surrogate.ElementTest;
import com.gstuer.modelmerging.utility.IdentifierGenerator;

public class SignatureTest extends ElementTest<Signature, OperationSignature> {
    @Override
    protected Signature createElement(OperationSignature value, boolean isPlaceholder) {
        return new Signature(value, isPlaceholder);
    }

    @Override
    protected OperationSignature getUniqueValue() {
        String identifier = IdentifierGenerator.getUniqueIdentifier();
        OperationSignature value = RepositoryFactory.eINSTANCE.createOperationSignature();
        value.setEntityName(identifier);
        return value;
    }

    @Override
    protected Signature getUniqueNonPlaceholder() {
        return new Signature(getUniqueValue(), false);
    }

    @Override
    protected Signature getPlaceholderOf(Signature replaceable) {
        return new Signature(replaceable.getValue(), true);
    }
}
