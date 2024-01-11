package org.palladiosimulator.somox.analyzer.rules.mocore.processor.relation;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Component;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Interface;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.InterfaceRequirementRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.utility.ElementFactory;

import tools.mdsd.mocore.framework.processor.RelationProcessorTest;

public class InterfaceRequirementRelationProcessorTest
        extends RelationProcessorTest<InterfaceRequirementRelationProcessor,
                PcmSurrogate, InterfaceRequirementRelation, Component<?>, Interface> {
    @Override
    protected InterfaceRequirementRelation createRelation(Component<?> source, Interface destination,
            boolean isPlaceholder) {
        return new InterfaceRequirementRelation(source, destination, isPlaceholder);
    }

    @Override
    protected Component<?> getUniqueNonPlaceholderSourceEntity() {
        return ElementFactory.createUniqueComponent(false);
    }

    @Override
    protected Component<?> getPlaceholderOfSourceEntity(Component<?> source) {
        return new Component<>(source.getValue(), true);
    }

    @Override
    protected Interface getUniqueNonPlaceholderDestinationEntity() {
        return ElementFactory.createUniqueInterface(false);
    }

    @Override
    protected Interface getPlaceholderOfDestinationEntity(Interface destination) {
        return new Interface(destination.getValue(), true);
    }

    @Override
    protected InterfaceRequirementRelationProcessor createProcessor(PcmSurrogate model) {
        return new InterfaceRequirementRelationProcessor(model);
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }
}
