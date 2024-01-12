package org.palladiosimulator.retriever.mocore.surrogate.element;

import org.palladiosimulator.generator.fluent.repository.factory.FluentRepositoryFactory;
import org.palladiosimulator.pcm.repository.BasicComponent;

public class AtomicComponent extends Component<BasicComponent> {
    public AtomicComponent(final BasicComponent value, final boolean isPlaceholder) {
        super(value, isPlaceholder);
    }

    public static AtomicComponent getNamedPlaceholder(final String name) {
        final BasicComponent value = new FluentRepositoryFactory().newBasicComponent()
            .withName(name)
            .build();
        return new AtomicComponent(value, true);
    }

    public static AtomicComponent getUniquePlaceholder() {
        final String identifier = "Placeholder_" + getUniqueValue();
        return getNamedPlaceholder(identifier);
    }
}
