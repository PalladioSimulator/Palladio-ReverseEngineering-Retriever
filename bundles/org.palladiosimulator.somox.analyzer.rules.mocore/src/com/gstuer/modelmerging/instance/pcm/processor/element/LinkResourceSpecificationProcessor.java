package com.gstuer.modelmerging.instance.pcm.processor.element;

import java.util.List;
import java.util.Objects;

import com.gstuer.modelmerging.framework.processor.Processor;
import com.gstuer.modelmerging.instance.pcm.surrogate.PcmSurrogate;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Deployment;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.LinkResourceSpecification;
import com.gstuer.modelmerging.instance.pcm.surrogate.relation.DeploymentDeploymentRelation;
import com.gstuer.modelmerging.instance.pcm.surrogate.relation.LinkResourceSpecificationRelation;

public class LinkResourceSpecificationProcessor extends Processor<PcmSurrogate, LinkResourceSpecification> {
    public LinkResourceSpecificationProcessor(PcmSurrogate model) {
        super(model, LinkResourceSpecification.class);
    }

    @Override
    protected void refine(LinkResourceSpecification discovery) {
        List<LinkResourceSpecificationRelation> relations = this.getModel()
                .getByType(LinkResourceSpecificationRelation.class);
        relations.removeIf(relation -> !Objects.equals(relation.getSource(), discovery));

        if (relations.isEmpty()) {
            Deployment sourcePlaceholder = Deployment.getUniquePlaceholder();
            Deployment destinationPlaceholder = Deployment.getUniquePlaceholder();
            DeploymentDeploymentRelation deploymentRelation = new DeploymentDeploymentRelation(sourcePlaceholder,
                    destinationPlaceholder, true);
            LinkResourceSpecificationRelation implicitRelation = new LinkResourceSpecificationRelation(discovery,
                    deploymentRelation, true);
            addImplication(implicitRelation);
        }
    }
}
