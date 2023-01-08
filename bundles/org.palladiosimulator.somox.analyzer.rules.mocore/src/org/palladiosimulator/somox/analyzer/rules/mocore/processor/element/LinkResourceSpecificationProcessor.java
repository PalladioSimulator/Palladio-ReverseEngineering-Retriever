package org.palladiosimulator.somox.analyzer.rules.mocore.processor.element;

import java.util.List;
import java.util.Objects;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Deployment;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.LinkResourceSpecification;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.DeploymentDeploymentRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.LinkResourceSpecificationRelation;

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
