package org.palladiosimulator.retriever.mocore.surrogate.element;

import org.palladiosimulator.generator.fluent.repository.factory.FluentRepositoryFactory;
import org.palladiosimulator.pcm.repository.BasicComponent;

import tools.mdsd.mocore.framework.surrogate.ElementTest;
import tools.mdsd.mocore.utility.IdentifierGenerator;

public class AtomicComponentTest extends ElementTest<AtomicComponent, BasicComponent> {
    @Override
    protected AtomicComponent createElement(final BasicComponent value, final boolean isPlaceholder) {
        return new AtomicComponent(value, isPlaceholder);
    }

    @Override
    protected BasicComponent getUniqueValue() {
        final String identifier = IdentifierGenerator.getUniqueIdentifier();
        final BasicComponent value = new FluentRepositoryFactory().newBasicComponent()
            .withName(identifier)
            .build();
        return value;
    }

    @Override
    protected AtomicComponent getUniqueNonPlaceholder() {
        return new AtomicComponent(this.getUniqueValue(), false);
    }

    @Override
    protected AtomicComponent getPlaceholderOf(final AtomicComponent replaceable) {
        return new AtomicComponent(replaceable.getValue(), true);
    }
}
