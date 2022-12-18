package com.gstuer.modelmerging.instance.pcm.processor.relation;

import com.gstuer.modelmerging.framework.processor.RelationProcessor;
import com.gstuer.modelmerging.instance.pcm.surrogate.PcmSurrogate;
import com.gstuer.modelmerging.instance.pcm.surrogate.relation.ComponentSignatureProvisionRelation;

public class ComponentSignatureProvisionRelationProcessor
        extends RelationProcessor<PcmSurrogate, ComponentSignatureProvisionRelation> {
    public ComponentSignatureProvisionRelationProcessor(PcmSurrogate model) {
        super(model, ComponentSignatureProvisionRelation.class);
    }
}
