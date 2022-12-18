package org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element;

import java.util.Objects;

import com.gstuer.modelmerging.framework.surrogate.Element;

import de.uka.ipd.sdq.identifier.Identifier;

public abstract class PcmElement<T extends Identifier> extends Element<T> {
    protected PcmElement(T value, boolean isPlaceholder) {
        super(value, isPlaceholder);
    }

    public String getIdentifier() {
        return getValue().getId();
    }

    @Override
    public int hashCode() {
        String wrappedIdentifier = getValue().getId();
        return Objects.hash(this.isPlaceholder(), wrappedIdentifier);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        PcmElement<?> element = (PcmElement<?>) object;
        return Objects.equals(this.getIdentifier(), element.getIdentifier())
                && (this.isPlaceholder() == element.isPlaceholder());
    }
}
