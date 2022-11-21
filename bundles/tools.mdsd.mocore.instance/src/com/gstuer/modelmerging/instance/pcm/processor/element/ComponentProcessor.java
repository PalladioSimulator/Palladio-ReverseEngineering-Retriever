package com.gstuer.modelmerging.instance.pcm.processor.element;

import java.util.List;

import com.gstuer.modelmerging.framework.processor.Processor;
import com.gstuer.modelmerging.instance.pcm.surrogate.PcmSurrogate;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Component;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Deployment;
import com.gstuer.modelmerging.instance.pcm.surrogate.relation.ComponentAllocationRelation;

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
