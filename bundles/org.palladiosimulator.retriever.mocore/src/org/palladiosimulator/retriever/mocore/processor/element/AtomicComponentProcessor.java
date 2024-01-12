package org.palladiosimulator.retriever.mocore.processor.element;

import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.AtomicComponent;

public class AtomicComponentProcessor extends ComponentProcessor<AtomicComponent> {
    public AtomicComponentProcessor(final PcmSurrogate model) {
        super(model, AtomicComponent.class);
    }
}
