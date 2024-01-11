package org.palladiosimulator.retriever.mocore.processor.relation;

import java.util.List;

import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.Component;
import org.palladiosimulator.retriever.mocore.surrogate.element.Composite;
import org.palladiosimulator.retriever.mocore.surrogate.relation.CompositeRequirementDelegationRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.CompositionRelation;

import tools.mdsd.mocore.framework.processor.RelationProcessor;

public class CompositeRequirementDelegationRelationProcessor
        extends RelationProcessor<PcmSurrogate, CompositeRequirementDelegationRelation> {
    public CompositeRequirementDelegationRelationProcessor(PcmSurrogate model) {
        super(model, CompositeRequirementDelegationRelation.class);
    }

    @Override
    protected void refine(CompositeRequirementDelegationRelation discovery) {
        Composite discoveryComposite = (Composite) discovery.getSource().getSource();
        Component<?> discoveryChild = discovery.getDestination().getSource();

        // Check if the sub-component is part of the composite already
        List<CompositionRelation> compositions = getModel().getByType(CompositionRelation.class);
        compositions.removeIf(relation -> !discoveryComposite.equals(relation.getSource()));
        compositions.removeIf(relation -> !discoveryChild.equals(relation.getDestination()));
        if (compositions.isEmpty()) {
            CompositionRelation composition = new CompositionRelation(discoveryComposite, discoveryChild, true);
            addImplication(composition);
        }

        super.refine(discovery);
    }
}
