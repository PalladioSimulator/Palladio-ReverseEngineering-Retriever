package org.palladiosimulator.somox.analyzer.rules.mocore.processor.relation;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Component;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Composite;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.CompositionRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.utility.ElementFactory;

import tools.mdsd.mocore.framework.processor.RelationProcessorTest;

public class CompositionRelationProcessorTest extends RelationProcessorTest<CompositionRelationProcessor,
        PcmSurrogate, CompositionRelation, Composite, Component<?>> {
    @Override
    protected CompositionRelation createRelation(Composite source, Component<?> destination,
            boolean isPlaceholder) {
        return new CompositionRelation(source, destination, isPlaceholder);
    }

    @Override
    protected Composite getUniqueNonPlaceholderSourceEntity() {
        return ElementFactory.createUniqueComposite(false);
    }

    @Override
    protected Composite getPlaceholderOfSourceEntity(Composite source) {
        return new Composite(source.getValue(), true);
    }

    @Override
    protected Component<?> getUniqueNonPlaceholderDestinationEntity() {
        return ElementFactory.createUniqueComponent(false);
    }

    @Override
    protected Component<?> getPlaceholderOfDestinationEntity(Component<?> destination) {
        return new Component<>(destination.getValue(), true);
    }

    @Override
    protected CompositionRelationProcessor createProcessor(PcmSurrogate model) {
        return new CompositionRelationProcessor(model);
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }
}
