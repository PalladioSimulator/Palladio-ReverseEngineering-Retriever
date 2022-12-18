package org.palladiosimulator.somox.analyzer.rules.mocore.processor.relation;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Component;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Interface;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Signature;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.ComponentSignatureProvisionRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.InterfaceProvisionRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.SignatureProvisionRelation;

import com.gstuer.modelmerging.framework.processor.RelationProcessorTest;

public class ComponentSignatureProvisionRelationProcessorTest
        extends RelationProcessorTest<ComponentSignatureProvisionRelationProcessor, PcmSurrogate,
                ComponentSignatureProvisionRelation, InterfaceProvisionRelation, SignatureProvisionRelation> {
    private static final Interface RELATION_INTERFACE = Interface.getUniquePlaceholder();

    @Override
    protected ComponentSignatureProvisionRelation createRelation(InterfaceProvisionRelation source,
            SignatureProvisionRelation destination, boolean isPlaceholder) {
        return new ComponentSignatureProvisionRelation(source, destination, isPlaceholder);
    }

    @Override
    protected InterfaceProvisionRelation getUniqueNonPlaceholderSourceEntity() {
        Component source = Component.getUniquePlaceholder();
        return new InterfaceProvisionRelation(source, RELATION_INTERFACE, false);
    }

    @Override
    protected InterfaceProvisionRelation getPlaceholderOfSourceEntity(InterfaceProvisionRelation source) {
        return new InterfaceProvisionRelation(source.getSource(), source.getDestination(), true);
    }

    @Override
    protected SignatureProvisionRelation getUniqueNonPlaceholderDestinationEntity() {
        Signature signature = Signature.getUniquePlaceholder();
        return new SignatureProvisionRelation(signature, RELATION_INTERFACE, false);
    }

    @Override
    protected SignatureProvisionRelation getPlaceholderOfDestinationEntity(SignatureProvisionRelation destination) {
        return new SignatureProvisionRelation(destination.getSource(), destination.getDestination(), true);
    }

    @Override
    protected ComponentSignatureProvisionRelationProcessor createProcessor(PcmSurrogate model) {
        return new ComponentSignatureProvisionRelationProcessor(model);
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }
}
