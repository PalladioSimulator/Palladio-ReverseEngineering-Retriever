package org.palladiosimulator.retriever.mocore.surrogate.element;

import org.palladiosimulator.pcm.resourceenvironment.CommunicationLinkResourceSpecification;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentFactory;

public class LinkResourceSpecification extends PcmElement<CommunicationLinkResourceSpecification> {
    public LinkResourceSpecification(CommunicationLinkResourceSpecification value, boolean isPlaceholder) {
        super(value, isPlaceholder);
    }

    public static LinkResourceSpecification getUniquePlaceholder() {
        String identifier = "Placeholder_" + getUniqueValue();
        double failureProbability = 0D;

        CommunicationLinkResourceSpecification value = ResourceenvironmentFactory.eINSTANCE
                .createCommunicationLinkResourceSpecification();
        value.setId(identifier);
        value.setFailureProbability(failureProbability);
        return new LinkResourceSpecification(value, true);
    }
}
