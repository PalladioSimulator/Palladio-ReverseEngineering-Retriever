package org.palladiosimulator.somox.analyzer.rules.mocore.processor.relation;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.ComponentAllocationRelation;

import com.gstuer.modelmerging.framework.processor.RelationProcessor;

public class ComponentAllocationRelationProcessor extends RelationProcessor<PcmSurrogate, ComponentAllocationRelation> {
    public ComponentAllocationRelationProcessor(PcmSurrogate model) {
        super(model, ComponentAllocationRelation.class);
    }
}
