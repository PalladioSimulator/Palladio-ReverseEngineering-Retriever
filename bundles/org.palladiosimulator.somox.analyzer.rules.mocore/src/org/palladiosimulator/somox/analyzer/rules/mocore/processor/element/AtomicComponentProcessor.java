package org.palladiosimulator.somox.analyzer.rules.mocore.processor.element;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.AtomicComponent;

public class AtomicComponentProcessor extends ComponentProcessor<AtomicComponent> {
    public AtomicComponentProcessor(PcmSurrogate model) {
        super(model, AtomicComponent.class);
    }
}
