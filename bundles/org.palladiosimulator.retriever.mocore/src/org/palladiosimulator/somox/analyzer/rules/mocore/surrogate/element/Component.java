package org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element;

import org.palladiosimulator.pcm.repository.RepositoryComponent;

public class Component<T extends RepositoryComponent> extends PcmElement<T> {
    public Component(T value, boolean isPlaceholder) {
        super(value, isPlaceholder);
    }

    public static Component<?> getNamedPlaceholder(String name) {
        return AtomicComponent.getNamedPlaceholder(name);
    }

    public static Component<?> getUniquePlaceholder() {
        return AtomicComponent.getUniquePlaceholder();
    }
}
