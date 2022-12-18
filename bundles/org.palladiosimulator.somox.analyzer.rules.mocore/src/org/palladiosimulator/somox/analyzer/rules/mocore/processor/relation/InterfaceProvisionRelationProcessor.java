package org.palladiosimulator.somox.analyzer.rules.mocore.processor.relation;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.InterfaceProvisionRelation;

import com.gstuer.modelmerging.framework.processor.RelationProcessor;

public class InterfaceProvisionRelationProcessor extends RelationProcessor<PcmSurrogate, InterfaceProvisionRelation> {
    public InterfaceProvisionRelationProcessor(PcmSurrogate model) {
        super(model, InterfaceProvisionRelation.class);
    }
}
