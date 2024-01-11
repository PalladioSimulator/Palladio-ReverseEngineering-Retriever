package org.palladiosimulator.retriever.mocore.surrogate.element;

import org.palladiosimulator.generator.fluent.repository.factory.FluentRepositoryFactory;
import org.palladiosimulator.pcm.repository.CompositeComponent;
import org.palladiosimulator.pcm.repository.RepositoryComponent;

public class Composite extends Component<CompositeComponent> {
    public Composite(CompositeComponent value, boolean isPlaceholder) {
        super(value, isPlaceholder);
    }

    public static Composite getUniquePlaceholder() {
        String identifier = "Placeholder_" + getUniqueValue();
        RepositoryComponent value = new FluentRepositoryFactory().newCompositeComponent().withName(identifier).build();
        return new Composite((CompositeComponent) value, true);
    }
}
