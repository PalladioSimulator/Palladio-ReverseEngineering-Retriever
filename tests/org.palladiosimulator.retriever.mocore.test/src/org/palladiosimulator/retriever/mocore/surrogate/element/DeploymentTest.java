package org.palladiosimulator.retriever.mocore.surrogate.element;

import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentFactory;

import tools.mdsd.mocore.framework.surrogate.ElementTest;
import tools.mdsd.mocore.utility.IdentifierGenerator;

public class DeploymentTest extends ElementTest<Deployment, ResourceContainer> {
    @Override
    protected Deployment createElement(final ResourceContainer value, final boolean isPlaceholder) {
        return new Deployment(value, isPlaceholder);
    }

    @Override
    protected ResourceContainer getUniqueValue() {
        final String identifier = IdentifierGenerator.getUniqueIdentifier();
        final ResourceContainer value = ResourceenvironmentFactory.eINSTANCE.createResourceContainer();
        value.setEntityName(identifier);
        return value;
    }

    @Override
    protected Deployment getUniqueNonPlaceholder() {
        return new Deployment(this.getUniqueValue(), false);
    }

    @Override
    protected Deployment getPlaceholderOf(final Deployment replaceable) {
        return new Deployment(replaceable.getValue(), true);
    }
}
