package org.palladiosimulator.somox.analyzer.rules.mocore.processor.element;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.AtomicComponent;

public class AtomicComponentProcessorTest extends ComponentProcessorTest<AtomicComponent> {
    @Override
    protected AtomicComponentProcessor createProcessor(PcmSurrogate model) {
        return new AtomicComponentProcessor(model);
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }

    @Override
    protected AtomicComponent createUniqueReplaceable() {
        return AtomicComponent.getUniquePlaceholder();
    }
}
