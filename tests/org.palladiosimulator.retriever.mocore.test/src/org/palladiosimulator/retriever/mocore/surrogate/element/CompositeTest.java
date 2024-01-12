package org.palladiosimulator.retriever.mocore.surrogate.element;

import org.palladiosimulator.generator.fluent.repository.factory.FluentRepositoryFactory;
import org.palladiosimulator.pcm.repository.CompositeComponent;
import org.palladiosimulator.pcm.repository.RepositoryComponent;

import tools.mdsd.mocore.framework.surrogate.ElementTest;
import tools.mdsd.mocore.utility.IdentifierGenerator;

public class CompositeTest extends ElementTest<Composite, CompositeComponent> {
    @Override
    protected Composite createElement(CompositeComponent value, boolean isPlaceholder) {
        return new Composite(value, isPlaceholder);
    }

    @Override
    protected CompositeComponent getUniqueValue() {
        String identifier = IdentifierGenerator.getUniqueIdentifier();
        RepositoryComponent value = new FluentRepositoryFactory().newCompositeComponent()
            .withName(identifier)
            .build();
        return (CompositeComponent) value;
    }

    @Override
    protected Composite getUniqueNonPlaceholder() {
        return new Composite(getUniqueValue(), false);
    }

    @Override
    protected Composite getPlaceholderOf(Composite replaceable) {
        return new Composite(replaceable.getValue(), true);
    }
}
