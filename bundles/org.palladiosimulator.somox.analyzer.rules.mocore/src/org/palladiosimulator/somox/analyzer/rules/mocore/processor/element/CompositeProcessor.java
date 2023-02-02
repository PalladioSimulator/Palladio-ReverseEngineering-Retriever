package org.palladiosimulator.somox.analyzer.rules.mocore.processor.element;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Composite;

public class CompositeProcessor extends ComponentProcessor<Composite> {
    public CompositeProcessor(PcmSurrogate model) {
        super(model, Composite.class);
    }
}
