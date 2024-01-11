package org.palladiosimulator.retriever.mocore.processor.relation;

import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.relation.ComponentSignatureProvisionRelation;

import tools.mdsd.mocore.framework.processor.RelationProcessor;

public class ComponentSignatureProvisionRelationProcessor
        extends RelationProcessor<PcmSurrogate, ComponentSignatureProvisionRelation> {
    public ComponentSignatureProvisionRelationProcessor(PcmSurrogate model) {
        super(model, ComponentSignatureProvisionRelation.class);
    }
}
