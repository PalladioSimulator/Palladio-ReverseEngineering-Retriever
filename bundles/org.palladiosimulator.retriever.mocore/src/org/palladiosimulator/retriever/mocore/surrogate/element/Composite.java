package org.palladiosimulator.retriever.mocore.surrogate.element;

import org.palladiosimulator.generator.fluent.repository.factory.FluentRepositoryFactory;
import org.palladiosimulator.pcm.repository.CompositeComponent;
import org.palladiosimulator.pcm.repository.RepositoryComponent;

public class Composite extends Component<CompositeComponent> {
    public Composite(final CompositeComponent value, final boolean isPlaceholder) {
        super(value, isPlaceholder);
    }

    public static Composite getUniquePlaceholder() {
        final String identifier = "Placeholder_" + getUniqueValue();
        final RepositoryComponent value = new FluentRepositoryFactory().newCompositeComponent()
            .withName(identifier)
            .build();
        return new Composite((CompositeComponent) value, true);
    }
}
