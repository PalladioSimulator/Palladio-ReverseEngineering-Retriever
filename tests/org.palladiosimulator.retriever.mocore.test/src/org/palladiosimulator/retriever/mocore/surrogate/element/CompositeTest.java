package org.palladiosimulator.retriever.mocore.surrogate.element;

import org.palladiosimulator.generator.fluent.repository.factory.FluentRepositoryFactory;
import org.palladiosimulator.pcm.repository.CompositeComponent;
import org.palladiosimulator.pcm.repository.RepositoryComponent;

import tools.mdsd.mocore.framework.surrogate.ElementTest;
import tools.mdsd.mocore.utility.IdentifierGenerator;

public class CompositeTest extends ElementTest<Composite, CompositeComponent> {
    @Override
    protected Composite createElement(final CompositeComponent value, final boolean isPlaceholder) {
        return new Composite(value, isPlaceholder);
    }

    @Override
    protected CompositeComponent getUniqueValue() {
        final String identifier = IdentifierGenerator.getUniqueIdentifier();
        final RepositoryComponent value = new FluentRepositoryFactory().newCompositeComponent()
            .withName(identifier)
            .build();
        return (CompositeComponent) value;
    }

    @Override
    protected Composite getUniqueNonPlaceholder() {
        return new Composite(this.getUniqueValue(), false);
    }

    @Override
    protected Composite getPlaceholderOf(final Composite replaceable) {
        return new Composite(replaceable.getValue(), true);
    }
}
