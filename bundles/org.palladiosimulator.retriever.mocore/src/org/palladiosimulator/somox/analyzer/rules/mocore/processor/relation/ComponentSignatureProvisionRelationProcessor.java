package org.palladiosimulator.somox.analyzer.rules.mocore.processor.relation;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.ComponentSignatureProvisionRelation;

import tools.mdsd.mocore.framework.processor.RelationProcessor;

public class ComponentSignatureProvisionRelationProcessor
        extends RelationProcessor<PcmSurrogate, ComponentSignatureProvisionRelation> {
    public ComponentSignatureProvisionRelationProcessor(PcmSurrogate model) {
        super(model, ComponentSignatureProvisionRelation.class);
    }
}
