package com.gstuer.modelmerging.instance.pcm.processor.relation;

import com.gstuer.modelmerging.framework.processor.RelationProcessor;
import com.gstuer.modelmerging.instance.pcm.surrogate.PcmSurrogate;
import com.gstuer.modelmerging.instance.pcm.surrogate.relation.InterfaceProvisionRelation;

public class InterfaceProvisionRelationProcessor extends RelationProcessor<PcmSurrogate, InterfaceProvisionRelation> {
    public InterfaceProvisionRelationProcessor(PcmSurrogate model) {
        super(model, InterfaceProvisionRelation.class);
    }
}
