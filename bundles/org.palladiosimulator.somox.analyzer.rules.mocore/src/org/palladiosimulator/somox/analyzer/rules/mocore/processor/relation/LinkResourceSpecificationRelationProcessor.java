package org.palladiosimulator.somox.analyzer.rules.mocore.processor.relation;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.LinkResourceSpecificationRelation;

import tools.mdsd.mocore.framework.processor.RelationProcessor;

public class LinkResourceSpecificationRelationProcessor
        extends RelationProcessor<PcmSurrogate, LinkResourceSpecificationRelation> {
    public LinkResourceSpecificationRelationProcessor(PcmSurrogate model) {
        super(model, LinkResourceSpecificationRelation.class);
    }
}
