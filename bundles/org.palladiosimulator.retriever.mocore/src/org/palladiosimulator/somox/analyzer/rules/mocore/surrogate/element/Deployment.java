package org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element;

import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentFactory;

public class Deployment extends PcmElement<ResourceContainer> {
    public Deployment(ResourceContainer value, boolean isPlaceholder) {
        super(value, isPlaceholder);
    }

    public static Deployment getUniquePlaceholder() {
        String identifier = "Placeholder_" + getUniqueValue();
        ResourceContainer value = ResourceenvironmentFactory.eINSTANCE.createResourceContainer();
        value.setEntityName(identifier);
        return new Deployment(value, true);
    }
}
