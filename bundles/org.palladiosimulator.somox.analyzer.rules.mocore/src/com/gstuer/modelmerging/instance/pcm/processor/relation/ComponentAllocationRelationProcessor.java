package com.gstuer.modelmerging.instance.pcm.processor.relation;

import com.gstuer.modelmerging.framework.processor.RelationProcessor;
import com.gstuer.modelmerging.instance.pcm.surrogate.PcmSurrogate;
import com.gstuer.modelmerging.instance.pcm.surrogate.relation.ComponentAllocationRelation;

public class ComponentAllocationRelationProcessor extends RelationProcessor<PcmSurrogate, ComponentAllocationRelation> {
    public ComponentAllocationRelationProcessor(PcmSurrogate model) {
        super(model, ComponentAllocationRelation.class);
    }
}
