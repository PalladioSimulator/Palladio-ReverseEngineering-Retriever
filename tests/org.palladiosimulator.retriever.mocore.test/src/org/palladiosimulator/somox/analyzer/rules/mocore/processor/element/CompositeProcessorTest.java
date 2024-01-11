package org.palladiosimulator.somox.analyzer.rules.mocore.processor.element;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Composite;

public class CompositeProcessorTest extends ComponentProcessorTest<Composite> {
    @Override
    protected CompositeProcessor createProcessor(PcmSurrogate model) {
        return new CompositeProcessor(model);
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }

    @Override
    protected Composite createUniqueReplaceable() {
        return Composite.getUniquePlaceholder();
    }
}
