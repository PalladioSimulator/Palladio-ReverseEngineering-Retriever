package org.palladiosimulator.retriever.mocore.surrogate.element;

import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentFactory;

public class Deployment extends PcmElement<ResourceContainer> {
    public Deployment(final ResourceContainer value, final boolean isPlaceholder) {
        super(value, isPlaceholder);
    }

    public static Deployment getUniquePlaceholder() {
        final String identifier = "Placeholder_" + getUniqueValue();
        final ResourceContainer value = ResourceenvironmentFactory.eINSTANCE.createResourceContainer();
        value.setEntityName(identifier);
        return new Deployment(value, true);
    }
}
