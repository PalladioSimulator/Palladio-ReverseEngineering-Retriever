package org.palladiosimulator.retriever.mocore.processor.relation;

import java.util.List;
import java.util.Objects;

import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.LinkResourceSpecification;
import org.palladiosimulator.retriever.mocore.surrogate.relation.DeploymentDeploymentRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.LinkResourceSpecificationRelation;

import tools.mdsd.mocore.framework.processor.RelationProcessor;

public class DeploymentDeploymentRelationProcessor extends RelationProcessor<PcmSurrogate, DeploymentDeploymentRelation> {
    public DeploymentDeploymentRelationProcessor(PcmSurrogate model) {
        super(model, DeploymentDeploymentRelation.class);
    }

    @Override
    protected void refine(DeploymentDeploymentRelation discovery) {
        // Check whether a linking resource specification already exists for the link to be merged
        List<LinkResourceSpecificationRelation> specifications = this.getModel()
                .getByType(LinkResourceSpecificationRelation.class);
        specifications.removeIf(specification -> !Objects.equals(discovery, specification.getDestination()));
        if (specifications.isEmpty()) {
            // Add implicit placeholder specification, if no specification is found for this deployment link
            LinkResourceSpecification placeholderSpecification = LinkResourceSpecification.getUniquePlaceholder();
            LinkResourceSpecificationRelation implicitRelation = new LinkResourceSpecificationRelation(
                    placeholderSpecification, discovery, true);
            this.addImplication(implicitRelation);
        }

        super.refine(discovery);
    }

    @Override
    protected void replaceIndirectPlaceholders(DeploymentDeploymentRelation discovery) {
        /*
         * TL;DR Indirect refinement of depl->depl relations is disabled because it leads to information loss.
         *
         * In a real system each component has to be allocated to a minimum of one container. Otherwise, the component
         * is not a working part of the running system. As a consequence, a placeholder container is created for each
         * component without a real container. This placeholder cannot be replaced by any depl->depl relation with the
         * same source or destination because the replacement would also imply that the component of the container is
         * deployed on the new "replacement" container. This may be true but is false in general. Therefore, the
         * replacement of the placeholder container has to be triggered from a lower architectural "layer".
         *
         * Example: Let's assume we have two components called A and B. When we add A and B to the model, we also add a
         * placeholder container A_Cont for A and B_Cont for B. Moreover, a component assembly called A->B is added to
         * the model leading to a A_Cont<->B_Cont deployment relation. The resulting model is in a hazard-state due to
         * the following problem: If we now add another deployment relation, B_Cont<->C_Cont for example, the indirect
         * refinement would assume that C_Cont and A_Cont are the same and initiate the replacement of A_Cont to C_Cont.
         * The implication of this replacement is that A and the components of C_Cont are deployed on the same
         * container. In general, this implication is not true. Consequently, a replacement of A_Cont may only be
         * initiated by a lower-level relation. In the PCM case this relation is the component allocation relation.
         */
    }
}
