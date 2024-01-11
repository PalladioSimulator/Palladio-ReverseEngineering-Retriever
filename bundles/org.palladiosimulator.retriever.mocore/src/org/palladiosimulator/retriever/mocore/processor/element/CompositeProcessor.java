package org.palladiosimulator.retriever.mocore.processor.element;

import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.Composite;

public class CompositeProcessor extends ComponentProcessor<Composite> {
    public CompositeProcessor(PcmSurrogate model) {
        super(model, Composite.class);
    }
}
