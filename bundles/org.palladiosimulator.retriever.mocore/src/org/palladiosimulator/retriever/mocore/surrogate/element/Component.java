package org.palladiosimulator.retriever.mocore.surrogate.element;

import org.palladiosimulator.pcm.repository.RepositoryComponent;

public class Component<T extends RepositoryComponent> extends PcmElement<T> {
    public Component(final T value, final boolean isPlaceholder) {
        super(value, isPlaceholder);
    }

    public static Component<?> getNamedPlaceholder(final String name) {
        return AtomicComponent.getNamedPlaceholder(name);
    }

    public static Component<?> getUniquePlaceholder() {
        return AtomicComponent.getUniquePlaceholder();
    }
}
