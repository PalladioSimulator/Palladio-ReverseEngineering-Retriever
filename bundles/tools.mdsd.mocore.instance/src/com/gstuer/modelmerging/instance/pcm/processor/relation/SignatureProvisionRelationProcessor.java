package com.gstuer.modelmerging.instance.pcm.processor.relation;

import com.gstuer.modelmerging.framework.processor.RelationProcessor;
import com.gstuer.modelmerging.instance.pcm.surrogate.PcmSurrogate;
import com.gstuer.modelmerging.instance.pcm.surrogate.relation.SignatureProvisionRelation;

public class SignatureProvisionRelationProcessor extends RelationProcessor<PcmSurrogate, SignatureProvisionRelation> {
    public SignatureProvisionRelationProcessor(PcmSurrogate model) {
        super(model, SignatureProvisionRelation.class);
    }
}
