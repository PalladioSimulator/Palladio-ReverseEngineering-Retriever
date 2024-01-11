package org.palladiosimulator.retriever.mocore.processor.relation;

import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.relation.ServiceEffectSpecificationRelation;

import tools.mdsd.mocore.framework.processor.RelationProcessor;

public class ServiceEffectSpecificationRelationProcessor extends RelationProcessor<PcmSurrogate, ServiceEffectSpecificationRelation> {
    public ServiceEffectSpecificationRelationProcessor(PcmSurrogate model) {
        super(model, ServiceEffectSpecificationRelation.class);
    }
}
