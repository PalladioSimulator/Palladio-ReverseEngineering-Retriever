package org.palladiosimulator.retriever.mocore.processor.relation;

import java.util.List;

import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.Component;
import org.palladiosimulator.retriever.mocore.surrogate.element.Composite;
import org.palladiosimulator.retriever.mocore.surrogate.relation.CompositeProvisionDelegationRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.CompositionRelation;

import tools.mdsd.mocore.framework.processor.RelationProcessor;

public class CompositeProvisionDelegationRelationProcessor
        extends RelationProcessor<PcmSurrogate, CompositeProvisionDelegationRelation> {
    public CompositeProvisionDelegationRelationProcessor(final PcmSurrogate model) {
        super(model, CompositeProvisionDelegationRelation.class);
    }

    @Override
    protected void refine(final CompositeProvisionDelegationRelation discovery) {
        final Composite discoveryComposite = (Composite) discovery.getSource()
            .getSource();
        final Component<?> discoveryChild = discovery.getDestination()
            .getSource();

        // Check if the sub-component is part of the composite already
        final List<CompositionRelation> compositions = this.getModel()
            .getByType(CompositionRelation.class);
        compositions.removeIf(relation -> !discoveryComposite.equals(relation.getSource()));
        compositions.removeIf(relation -> !discoveryChild.equals(relation.getDestination()));
        if (compositions.isEmpty()) {
            final CompositionRelation composition = new CompositionRelation(discoveryComposite, discoveryChild, true);
            this.addImplication(composition);
        }

        super.refine(discovery);
    }
}
