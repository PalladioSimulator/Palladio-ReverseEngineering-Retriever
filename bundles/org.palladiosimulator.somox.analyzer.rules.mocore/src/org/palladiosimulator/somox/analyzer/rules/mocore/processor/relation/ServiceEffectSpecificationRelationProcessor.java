package org.palladiosimulator.somox.analyzer.rules.mocore.processor.relation;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.ServiceEffectSpecificationRelation;

import com.gstuer.modelmerging.framework.processor.RelationProcessor;

public class ServiceEffectSpecificationRelationProcessor extends RelationProcessor<PcmSurrogate, ServiceEffectSpecificationRelation> {
    public ServiceEffectSpecificationRelationProcessor(PcmSurrogate model) {
        super(model, ServiceEffectSpecificationRelation.class);
    }
}
