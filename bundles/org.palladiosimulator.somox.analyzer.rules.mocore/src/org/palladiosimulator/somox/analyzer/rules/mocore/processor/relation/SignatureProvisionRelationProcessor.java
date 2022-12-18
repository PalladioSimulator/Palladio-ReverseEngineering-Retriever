package org.palladiosimulator.somox.analyzer.rules.mocore.processor.relation;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.SignatureProvisionRelation;

import com.gstuer.modelmerging.framework.processor.RelationProcessor;

public class SignatureProvisionRelationProcessor extends RelationProcessor<PcmSurrogate, SignatureProvisionRelation> {
    public SignatureProvisionRelationProcessor(PcmSurrogate model) {
        super(model, SignatureProvisionRelation.class);
    }
}
