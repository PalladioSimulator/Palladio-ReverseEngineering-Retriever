package org.palladiosimulator.retriever.mocore.processor.element;

import org.palladiosimulator.retriever.mocore.processor.element.CompositeProcessor;
import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.Composite;

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
