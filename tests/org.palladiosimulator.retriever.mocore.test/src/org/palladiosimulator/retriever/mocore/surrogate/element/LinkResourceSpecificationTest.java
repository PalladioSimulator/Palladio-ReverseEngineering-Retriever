package org.palladiosimulator.retriever.mocore.surrogate.element;

import org.palladiosimulator.pcm.resourceenvironment.CommunicationLinkResourceSpecification;

import tools.mdsd.mocore.framework.surrogate.ElementTest;

public class LinkResourceSpecificationTest
        extends ElementTest<LinkResourceSpecification, CommunicationLinkResourceSpecification> {
    @Override
    protected LinkResourceSpecification createElement(final CommunicationLinkResourceSpecification value,
            final boolean isPlaceholder) {
        return new LinkResourceSpecification(value, isPlaceholder);
    }

    @Override
    protected CommunicationLinkResourceSpecification getUniqueValue() {
        return LinkResourceSpecification.getUniquePlaceholder()
            .getValue();
    }

    @Override
    protected LinkResourceSpecification getUniqueNonPlaceholder() {
        return new LinkResourceSpecification(this.getUniqueValue(), false);
    }

    @Override
    protected LinkResourceSpecification getPlaceholderOf(final LinkResourceSpecification replaceable) {
        return new LinkResourceSpecification(replaceable.getValue(), true);
    }
}
