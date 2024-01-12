package org.palladiosimulator.retriever.mocore.surrogate.element;

import java.util.Objects;

import de.uka.ipd.sdq.identifier.Identifier;
import tools.mdsd.mocore.framework.surrogate.Element;

public abstract class PcmElement<T extends Identifier> extends Element<T> {
    protected PcmElement(final T value, final boolean isPlaceholder) {
        super(value, isPlaceholder);
    }

    public String getIdentifier() {
        return this.getValue()
            .getId();
    }

    @Override
    public int hashCode() {
        final String wrappedIdentifier = this.getValue()
            .getId();
        return Objects.hash(this.isPlaceholder(), wrappedIdentifier);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        final PcmElement<?> element = (PcmElement<?>) object;
        return Objects.equals(this.getIdentifier(), element.getIdentifier())
                && (this.isPlaceholder() == element.isPlaceholder());
    }
}
