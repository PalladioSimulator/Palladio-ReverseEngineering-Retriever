package org.palladiosimulator.retriever.mocore.processor.relation;

import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.relation.CompositionRelation;

import tools.mdsd.mocore.framework.processor.RelationProcessor;

public class CompositionRelationProcessor extends RelationProcessor<PcmSurrogate, CompositionRelation> {
    public CompositionRelationProcessor(final PcmSurrogate model) {
        super(model, CompositionRelation.class);
    }
}
