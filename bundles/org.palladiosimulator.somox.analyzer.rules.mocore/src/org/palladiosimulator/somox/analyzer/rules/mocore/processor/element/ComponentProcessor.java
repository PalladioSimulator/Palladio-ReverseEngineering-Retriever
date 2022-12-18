package org.palladiosimulator.somox.analyzer.rules.mocore.processor.element;

import java.util.List;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Component;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Deployment;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.ComponentAllocationRelation;

import com.gstuer.modelmerging.framework.processor.Processor;

public class ComponentProcessor extends Processor<PcmSurrogate, Component> {
    public ComponentProcessor(PcmSurrogate model) {
        super(model, Component.class);
    }

    @Override
    protected void refine(Component discovery) {
        List<ComponentAllocationRelation> deploymentRelations = getModel().getByType(ComponentAllocationRelation.class);
        deploymentRelations.removeIf(relation -> !relation.getSource().equals(discovery));

        if (deploymentRelations.isEmpty()) {
            Deployment deployment = Deployment.getUniquePlaceholder();
            ComponentAllocationRelation relation = new ComponentAllocationRelation(discovery, deployment, true);
            addImplication(relation);
        }
    }
}
