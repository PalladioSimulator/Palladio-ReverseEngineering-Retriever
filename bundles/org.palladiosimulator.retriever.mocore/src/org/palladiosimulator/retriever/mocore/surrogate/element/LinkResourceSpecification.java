package org.palladiosimulator.retriever.mocore.surrogate.element;

import org.palladiosimulator.pcm.resourceenvironment.CommunicationLinkResourceSpecification;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentFactory;

public class LinkResourceSpecification extends PcmElement<CommunicationLinkResourceSpecification> {
    public LinkResourceSpecification(final CommunicationLinkResourceSpecification value, final boolean isPlaceholder) {
        super(value, isPlaceholder);
    }

    public static LinkResourceSpecification getUniquePlaceholder() {
        final String identifier = "Placeholder_" + getUniqueValue();
        final double failureProbability = 0D;

        final CommunicationLinkResourceSpecification value = ResourceenvironmentFactory.eINSTANCE
            .createCommunicationLinkResourceSpecification();
        value.setId(identifier);
        value.setFailureProbability(failureProbability);
        return new LinkResourceSpecification(value, true);
    }
}
