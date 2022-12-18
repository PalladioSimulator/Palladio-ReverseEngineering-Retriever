package org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element;

import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentFactory;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Deployment;

import com.gstuer.modelmerging.framework.surrogate.ElementTest;
import com.gstuer.modelmerging.utility.IdentifierGenerator;

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
