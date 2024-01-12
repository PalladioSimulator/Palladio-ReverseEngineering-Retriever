package org.palladiosimulator.retriever.mocore.processor.element;

import java.util.List;

import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.Component;
import org.palladiosimulator.retriever.mocore.surrogate.element.Deployment;
import org.palladiosimulator.retriever.mocore.surrogate.relation.ComponentAllocationRelation;

import tools.mdsd.mocore.framework.processor.Processor;

public abstract class ComponentProcessor<T extends Component<?>> extends Processor<PcmSurrogate, T> {
    public ComponentProcessor(PcmSurrogate model, Class<T> processableType) {
        super(model, processableType);
    }

    @Override
    protected void refine(T discovery) {
        List<ComponentAllocationRelation> deploymentRelations = getModel().getByType(ComponentAllocationRelation.class);
        deploymentRelations.removeIf(relation -> !relation.getSource()
            .equals(discovery));

        if (deploymentRelations.isEmpty()) {
            Deployment deployment = Deployment.getUniquePlaceholder();
            ComponentAllocationRelation relation = new ComponentAllocationRelation(discovery, deployment, true);
            addImplication(relation);
        }
    }
}
