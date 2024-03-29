package org.palladiosimulator.retriever.mocore.surrogate.element;

import org.palladiosimulator.generator.fluent.repository.factory.FluentRepositoryFactory;
import org.palladiosimulator.pcm.repository.OperationInterface;

import tools.mdsd.mocore.framework.surrogate.ElementTest;
import tools.mdsd.mocore.utility.IdentifierGenerator;

public class InterfaceTest extends ElementTest<Interface, OperationInterface> {
    @Override
    protected Interface createElement(final OperationInterface value, final boolean isPlaceholder) {
        return new Interface(value, isPlaceholder);
    }

    @Override
    protected OperationInterface getUniqueValue() {
        final String identifier = IdentifierGenerator.getUniqueIdentifier();
        final OperationInterface value = new FluentRepositoryFactory().newOperationInterface()
            .withName(identifier)
            .build();
        return value;
    }

    @Override
    protected Interface getUniqueNonPlaceholder() {
        return new Interface(this.getUniqueValue(), false);
    }

    @Override
    protected Interface getPlaceholderOf(final Interface replaceable) {
        return new Interface(replaceable.getValue(), true);
    }
}
