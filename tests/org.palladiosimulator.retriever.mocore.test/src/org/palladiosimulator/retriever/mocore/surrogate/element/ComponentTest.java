package org.palladiosimulator.retriever.mocore.surrogate.element;

import org.palladiosimulator.generator.fluent.repository.factory.FluentRepositoryFactory;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.RepositoryComponent;
import org.palladiosimulator.retriever.mocore.surrogate.element.Component;

import tools.mdsd.mocore.framework.surrogate.ElementTest;
import tools.mdsd.mocore.utility.IdentifierGenerator;

public class ComponentTest extends ElementTest<Component<RepositoryComponent>, RepositoryComponent> {
    @Override
    protected Component<RepositoryComponent> createElement(RepositoryComponent value, boolean isPlaceholder) {
        return new Component<>(value, isPlaceholder);
    }

    @Override
    protected RepositoryComponent getUniqueValue() {
        String identifier = IdentifierGenerator.getUniqueIdentifier();
        BasicComponent value = new FluentRepositoryFactory().newBasicComponent()
            .withName(identifier)
            .build();
        return value;
    }

    @Override
    protected Component<RepositoryComponent> getUniqueNonPlaceholder() {
        return new Component<>(getUniqueValue(), false);
    }

    @Override
    protected Component<RepositoryComponent> getPlaceholderOf(Component<RepositoryComponent> replaceable) {
        return new Component<>(replaceable.getValue(), true);
    }
}
