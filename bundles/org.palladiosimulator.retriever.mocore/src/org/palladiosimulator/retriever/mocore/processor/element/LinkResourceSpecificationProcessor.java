package org.palladiosimulator.retriever.mocore.processor.element;

import java.util.List;
import java.util.Objects;

import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.Deployment;
import org.palladiosimulator.retriever.mocore.surrogate.element.LinkResourceSpecification;
import org.palladiosimulator.retriever.mocore.surrogate.relation.DeploymentDeploymentRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.LinkResourceSpecificationRelation;

import tools.mdsd.mocore.framework.processor.Processor;

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
