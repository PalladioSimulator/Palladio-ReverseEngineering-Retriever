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
    public LinkResourceSpecificationProcessor(final PcmSurrogate model) {
        super(model, LinkResourceSpecification.class);
    }

    @Override
    protected void refine(final LinkResourceSpecification discovery) {
        final List<LinkResourceSpecificationRelation> relations = this.getModel()
            .getByType(LinkResourceSpecificationRelation.class);
        relations.removeIf(relation -> !Objects.equals(relation.getSource(), discovery));

        if (relations.isEmpty()) {
            final Deployment sourcePlaceholder = Deployment.getUniquePlaceholder();
            final Deployment destinationPlaceholder = Deployment.getUniquePlaceholder();
            final DeploymentDeploymentRelation deploymentRelation = new DeploymentDeploymentRelation(sourcePlaceholder,
                    destinationPlaceholder, true);
            final LinkResourceSpecificationRelation implicitRelation = new LinkResourceSpecificationRelation(discovery,
                    deploymentRelation, true);
            this.addImplication(implicitRelation);
        }
    }
}
