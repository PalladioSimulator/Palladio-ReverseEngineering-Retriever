package org.palladiosimulator.somox.analyzer.rules.mocore.processor.relation;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.InterfaceRequirementRelation;

import com.gstuer.modelmerging.framework.processor.RelationProcessor;

public class InterfaceRequirementRelationProcessor extends RelationProcessor<PcmSurrogate, InterfaceRequirementRelation> {
    public InterfaceRequirementRelationProcessor(PcmSurrogate model) {
        super(model, InterfaceRequirementRelation.class);
    }
}
