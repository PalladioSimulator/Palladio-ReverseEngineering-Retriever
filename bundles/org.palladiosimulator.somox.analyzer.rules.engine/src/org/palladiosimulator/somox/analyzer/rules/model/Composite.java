package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class Composite {

    private final String name;
    private final Set<Component> parts;
    private final Set<OperationInterface> internalInterfaces;
    private final Set<EntireInterface> requirements;
    private final Set<OperationInterface> provisions;

    public Composite(String name, Set<Component> parts, Set<EntireInterface> requirements,
            Set<OperationInterface> provisions, Set<OperationInterface> internalInterfaces) {
        this.name = name;
        this.parts = parts;
        this.internalInterfaces = internalInterfaces;
        this.requirements = requirements;
        this.provisions = provisions;
    }

    public String name() {
        return name;
    }

    public Set<EntireInterface> requirements() {
        return requirements;
    }

    public Set<OperationInterface> provisions() {
        return provisions;
    }

    public Set<Component> parts() {
        return Collections.unmodifiableSet(parts);
    }

    public Set<OperationInterface> internalInterfaces() {
        return Collections.unmodifiableSet(internalInterfaces);
    }

    public boolean isSubsetOf(final Composite other) {
        return other.parts()
            .containsAll(parts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(internalInterfaces, name, parts, provisions, requirements);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Composite other = (Composite) obj;
        return Objects.equals(internalInterfaces, other.internalInterfaces) && Objects.equals(name, other.name)
                && Objects.equals(parts, other.parts) && Objects.equals(provisions, other.provisions)
                && Objects.equals(requirements, other.requirements);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Name: ");
        builder.append(name);
        builder.append("\nRequirements:\n\t");
        builder.append(requirements.toString()
            .replace("\n", "\n\t"));
        builder.append("\nProvisions:\n\t");
        builder.append(provisions.toString()
            .replace("\n", "\n\t"));
        builder.append("\nInternal interfaces:\n");
        internalInterfaces.forEach(x -> builder.append('\t')
            .append(x.toString()
                .replace("\n", "\n\t"))
            .append('\n'));
        builder.append("\nParts:\n");
        parts.forEach(x -> builder.append('\t')
            .append(x.toString()
                .replace("\n", "\n\t"))
            .append('\n'));
        return builder.toString();
    }
}
