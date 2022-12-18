package com.gstuer.modelmerging.instance.pcm.processor.relation;

import com.gstuer.modelmerging.framework.processor.RelationProcessor;
import com.gstuer.modelmerging.instance.pcm.surrogate.PcmSurrogate;
import com.gstuer.modelmerging.instance.pcm.surrogate.relation.InterfaceRequirementRelation;

public class InterfaceRequirementRelationProcessor extends RelationProcessor<PcmSurrogate, InterfaceRequirementRelation> {
    public InterfaceRequirementRelationProcessor(PcmSurrogate model) {
        super(model, InterfaceRequirementRelation.class);
    }
}
