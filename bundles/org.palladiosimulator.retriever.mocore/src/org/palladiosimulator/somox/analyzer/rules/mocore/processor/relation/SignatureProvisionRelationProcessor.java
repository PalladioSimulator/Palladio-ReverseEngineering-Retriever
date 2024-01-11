package org.palladiosimulator.somox.analyzer.rules.mocore.processor.relation;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.SignatureProvisionRelation;

import tools.mdsd.mocore.framework.processor.RelationProcessor;

public class SignatureProvisionRelationProcessor extends RelationProcessor<PcmSurrogate, SignatureProvisionRelation> {
    public SignatureProvisionRelationProcessor(PcmSurrogate model) {
        super(model, SignatureProvisionRelation.class);
    }
}
