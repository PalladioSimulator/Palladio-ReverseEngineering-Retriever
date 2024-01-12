package org.palladiosimulator.retriever.mocore.processor.relation;

import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.Component;
import org.palladiosimulator.retriever.mocore.surrogate.element.Composite;
import org.palladiosimulator.retriever.mocore.surrogate.relation.CompositionRelation;
import org.palladiosimulator.retriever.mocore.utility.ElementFactory;

import tools.mdsd.mocore.framework.processor.RelationProcessorTest;

public class CompositionRelationProcessorTest extends
        RelationProcessorTest<CompositionRelationProcessor, PcmSurrogate, CompositionRelation, Composite, Component<?>> {
    @Override
    protected CompositionRelation createRelation(final Composite source, final Component<?> destination,
            final boolean isPlaceholder) {
        return new CompositionRelation(source, destination, isPlaceholder);
    }

    @Override
    protected Composite getUniqueNonPlaceholderSourceEntity() {
        return ElementFactory.createUniqueComposite(false);
    }

    @Override
    protected Composite getPlaceholderOfSourceEntity(final Composite source) {
        return new Composite(source.getValue(), true);
    }

    @Override
    protected Component<?> getUniqueNonPlaceholderDestinationEntity() {
        return ElementFactory.createUniqueComponent(false);
    }

    @Override
    protected Component<?> getPlaceholderOfDestinationEntity(final Component<?> destination) {
        return new Component<>(destination.getValue(), true);
    }

    @Override
    protected CompositionRelationProcessor createProcessor(final PcmSurrogate model) {
        return new CompositionRelationProcessor(model);
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }
}
