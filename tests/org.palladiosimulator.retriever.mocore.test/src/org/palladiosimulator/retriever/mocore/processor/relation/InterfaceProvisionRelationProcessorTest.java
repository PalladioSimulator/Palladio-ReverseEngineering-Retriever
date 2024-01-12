package org.palladiosimulator.retriever.mocore.processor.relation;

import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.Component;
import org.palladiosimulator.retriever.mocore.surrogate.element.Interface;
import org.palladiosimulator.retriever.mocore.surrogate.relation.InterfaceProvisionRelation;
import org.palladiosimulator.retriever.mocore.utility.ElementFactory;

import tools.mdsd.mocore.framework.processor.RelationProcessorTest;

public class InterfaceProvisionRelationProcessorTest extends
        RelationProcessorTest<InterfaceProvisionRelationProcessor, PcmSurrogate, InterfaceProvisionRelation, Component<?>, Interface> {
    @Override
    protected InterfaceProvisionRelation createRelation(final Component<?> source, final Interface destination,
            final boolean isPlaceholder) {
        return new InterfaceProvisionRelation(source, destination, isPlaceholder);
    }

    @Override
    protected Component<?> getUniqueNonPlaceholderSourceEntity() {
        return ElementFactory.createUniqueComponent(false);
    }

    @Override
    protected Component<?> getPlaceholderOfSourceEntity(final Component<?> source) {
        return new Component<>(source.getValue(), true);
    }

    @Override
    protected Interface getUniqueNonPlaceholderDestinationEntity() {
        return ElementFactory.createUniqueInterface(false);
    }

    @Override
    protected Interface getPlaceholderOfDestinationEntity(final Interface destination) {
        return new Interface(destination.getValue(), true);
    }

    @Override
    protected InterfaceProvisionRelationProcessor createProcessor(final PcmSurrogate model) {
        return new InterfaceProvisionRelationProcessor(model);
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }
}
