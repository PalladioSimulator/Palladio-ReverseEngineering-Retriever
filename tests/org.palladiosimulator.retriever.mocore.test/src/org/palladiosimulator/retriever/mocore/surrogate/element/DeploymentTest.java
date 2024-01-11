package org.palladiosimulator.retriever.mocore.surrogate.element;

import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentFactory;
import org.palladiosimulator.retriever.mocore.surrogate.element.Deployment;

import tools.mdsd.mocore.framework.surrogate.ElementTest;
import tools.mdsd.mocore.utility.IdentifierGenerator;

public class DeploymentTest extends ElementTest<Deployment, ResourceContainer> {
    @Override
    protected Deployment createElement(ResourceContainer value, boolean isPlaceholder) {
        return new Deployment(value, isPlaceholder);
    }

    @Override
    protected ResourceContainer getUniqueValue() {
        String identifier = IdentifierGenerator.getUniqueIdentifier();
        ResourceContainer value = ResourceenvironmentFactory.eINSTANCE.createResourceContainer();
        value.setEntityName(identifier);
        return value;
    }

    @Override
    protected Deployment getUniqueNonPlaceholder() {
        return new Deployment(getUniqueValue(), false);
    }

    @Override
    protected Deployment getPlaceholderOf(Deployment replaceable) {
        return new Deployment(replaceable.getValue(), true);
    }
}
