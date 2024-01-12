package org.palladiosimulator.retriever.mocore.surrogate.element;

import org.palladiosimulator.generator.fluent.repository.factory.FluentRepositoryFactory;
import org.palladiosimulator.pcm.repository.OperationInterface;

public class Interface extends PcmElement<OperationInterface> {
    public Interface(final OperationInterface value, final boolean isPlaceholder) {
        super(value, isPlaceholder);
    }

    public static Interface getUniquePlaceholder() {
        final String identifier = "Placeholder_" + getUniqueValue();
        final OperationInterface value = new FluentRepositoryFactory().newOperationInterface()
            .withName(identifier)
            .build();
        return new Interface(value, true);
    }
}
