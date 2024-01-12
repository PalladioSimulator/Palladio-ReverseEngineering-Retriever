package org.palladiosimulator.retriever.mocore.transformation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.palladiosimulator.retriever.mocore.utility.PcmEvaluationUtility.containsRepresentative;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.Deployment;
import org.palladiosimulator.retriever.mocore.surrogate.element.LinkResourceSpecification;
import org.palladiosimulator.retriever.mocore.surrogate.relation.DeploymentDeploymentRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.LinkResourceSpecificationRelation;

import tools.mdsd.mocore.framework.transformation.TransformerTest;

public class ResourceEnvironmentTransformerTest
        extends TransformerTest<ResourceEnvironmentTransformer, PcmSurrogate, ResourceEnvironment> {
    @Test
    public void testTransformIndependentPlaceholderContainers() {
        // Test data
        final ResourceEnvironmentTransformer transformer = this.createTransformer();
        final PcmSurrogate model = this.createEmptyModel();
        final Deployment fstDeployment = Deployment.getUniquePlaceholder();
        final Deployment sndDeployment = Deployment.getUniquePlaceholder();
        final Deployment trdDeployment = Deployment.getUniquePlaceholder();
        final Deployment fthDeployment = Deployment.getUniquePlaceholder();

        model.add(fstDeployment);
        model.add(sndDeployment);
        model.add(trdDeployment);
        model.add(fthDeployment);

        // Execution
        final ResourceEnvironment environment = transformer.transform(model);

        // Assertion
        assertNotNull(environment);
        assertTrue(containsRepresentative(environment, fstDeployment));
        assertTrue(containsRepresentative(environment, sndDeployment));
        assertTrue(containsRepresentative(environment, trdDeployment));
        assertTrue(containsRepresentative(environment, fthDeployment));
    }

    @Test
    public void testTransformConnectedPlaceholderContainers() {
        // Test data
        final ResourceEnvironmentTransformer transformer = this.createTransformer();
        final PcmSurrogate model = this.createEmptyModel();
        final Deployment fstDeployment = Deployment.getUniquePlaceholder();
        final Deployment sndDeployment = Deployment.getUniquePlaceholder();
        final Deployment trdDeployment = Deployment.getUniquePlaceholder();

        final DeploymentDeploymentRelation fstLinkRelation = new DeploymentDeploymentRelation(fstDeployment,
                sndDeployment, false);
        final DeploymentDeploymentRelation sndLinkRelation = new DeploymentDeploymentRelation(sndDeployment,
                trdDeployment, true);

        final LinkResourceSpecification fstLinkSpecification = LinkResourceSpecification.getUniquePlaceholder();
        final LinkResourceSpecificationRelation fstLinkSpecificationRelation = new LinkResourceSpecificationRelation(
                fstLinkSpecification, fstLinkRelation, false);

        final LinkResourceSpecification sndLinkSpecification = LinkResourceSpecification.getUniquePlaceholder();
        final LinkResourceSpecificationRelation sndLinkSpecificationRelation = new LinkResourceSpecificationRelation(
                sndLinkSpecification, sndLinkRelation, true);

        model.add(fstDeployment);
        model.add(sndDeployment);
        model.add(trdDeployment);
        model.add(fstLinkRelation);
        model.add(sndLinkRelation);
        model.add(fstLinkSpecificationRelation);
        model.add(sndLinkSpecificationRelation);

        // Execution
        final ResourceEnvironment environment = transformer.transform(model);

        // Assertion
        assertNotNull(environment);
        assertTrue(containsRepresentative(environment, fstDeployment));
        assertTrue(containsRepresentative(environment, sndDeployment));
        assertTrue(containsRepresentative(environment, trdDeployment));
        assertTrue(containsRepresentative(environment, fstLinkSpecificationRelation.getSource(),
                List.of(fstLinkSpecificationRelation.getDestination()
                    .getSource(),
                        fstLinkSpecificationRelation.getDestination()
                            .getDestination())));
        assertTrue(containsRepresentative(environment, sndLinkSpecificationRelation.getSource(),
                List.of(sndLinkSpecificationRelation.getDestination()
                    .getSource(),
                        sndLinkSpecificationRelation.getDestination()
                            .getDestination())));
    }

    @Override
    protected ResourceEnvironmentTransformer createTransformer() {
        return new ResourceEnvironmentTransformer();
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }
}
