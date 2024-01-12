package org.palladiosimulator.retriever.extraction.commonalities;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class Composite {

    private final String name;
    private final Set<Component> parts;
    private final Set<OperationInterface> internalInterfaces;
    private final Set<OperationInterface> requirements;
    private final Set<OperationInterface> provisions;

    public Composite(final String name, final Set<Component> parts, final Set<OperationInterface> requirements,
            final Set<OperationInterface> provisions, final Set<OperationInterface> internalInterfaces) {
        this.name = name;
        this.parts = parts;
        this.internalInterfaces = internalInterfaces;
        this.requirements = requirements;
        this.provisions = provisions;
    }

    public String name() {
        return this.name;
    }

    public Set<OperationInterface> requirements() {
        return this.requirements;
    }

    public Set<OperationInterface> provisions() {
        return this.provisions;
    }

    public Set<Component> parts() {
        return Collections.unmodifiableSet(this.parts);
    }

    public Set<OperationInterface> internalInterfaces() {
        return Collections.unmodifiableSet(this.internalInterfaces);
    }

    public boolean isSubsetOf(final Composite other) {
        return other.parts()
            .containsAll(this.parts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.internalInterfaces, this.name, this.parts, this.provisions, this.requirements);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (this.getClass() != obj.getClass())) {
            return false;
        }
        final Composite other = (Composite) obj;
        return Objects.equals(this.internalInterfaces, other.internalInterfaces)
                && Objects.equals(this.name, other.name) && Objects.equals(this.parts, other.parts)
                && Objects.equals(this.provisions, other.provisions)
                && Objects.equals(this.requirements, other.requirements);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Name: ");
        builder.append(this.name);
        builder.append("\nRequirements:\n\t");
        builder.append(this.requirements.toString()
            .replace("\n", "\n\t"));
        builder.append("\nProvisions:\n\t");
        builder.append(this.provisions.toString()
            .replace("\n", "\n\t"));
        builder.append("\nInternal interfaces:\n");
        this.internalInterfaces.forEach(x -> builder.append('\t')
            .append(x.toString()
                .replace("\n", "\n\t"))
            .append('\n'));
        builder.append("\nParts:\n");
        this.parts.forEach(x -> builder.append('\t')
            .append(x.toString()
                .replace("\n", "\n\t"))
            .append('\n'));
        return builder.toString();
    }
}
