package org.palladiosimulator.somox.analyzer.rules.mocore.processor.relation;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.CompositionRelation;

import tools.mdsd.mocore.framework.processor.RelationProcessor;

public class CompositionRelationProcessor extends RelationProcessor<PcmSurrogate, CompositionRelation> {
    public CompositionRelationProcessor(PcmSurrogate model) {
        super(model, CompositionRelation.class);
    }
}
