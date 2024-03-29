package org.palladiosimulator.retriever.mocore.processor.relation;

import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.relation.LinkResourceSpecificationRelation;

import tools.mdsd.mocore.framework.processor.RelationProcessor;

public class LinkResourceSpecificationRelationProcessor
        extends RelationProcessor<PcmSurrogate, LinkResourceSpecificationRelation> {
    public LinkResourceSpecificationRelationProcessor(final PcmSurrogate model) {
        super(model, LinkResourceSpecificationRelation.class);
    }
}
