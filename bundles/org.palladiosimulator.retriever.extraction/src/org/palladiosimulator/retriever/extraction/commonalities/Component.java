package org.palladiosimulator.retriever.extraction.commonalities;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * Components are {@code CompilationUnits}. They provide and require interfaces.
 *
 * @see CompilationUnit
 * @author Florian Bossert
 */
public class Component {
    private final CompUnitOrName compUnitOrName;
    private final Requirements requirements;
    private final Provisions provisions;

    public Component(final CompUnitOrName compUnitOrName, final Requirements requirements,
            final Provisions provisions) {
        this.compUnitOrName = compUnitOrName;
        this.requirements = requirements;
        this.provisions = provisions;
    }

    public Requirements requirements() {
        return this.requirements;
    }

    public Provisions provisions() {
        return this.provisions;
    }

    public Optional<CompilationUnit> compilationUnit() {
        return this.compUnitOrName.compilationUnit();
    }

    public String name() {
        return this.compUnitOrName.name();
    }

    public CompUnitOrName identifier() {
        return this.compUnitOrName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.compUnitOrName, this.provisions, this.requirements);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (this.getClass() != obj.getClass())) {
            return false;
        }
        final Component other = (Component) obj;
        return Objects.equals(this.compUnitOrName, other.compUnitOrName)
                && Objects.equals(this.provisions, other.provisions)
                && Objects.equals(this.requirements, other.requirements);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Name: ");
        builder.append(this.name());
        builder.append("\nRequirements:\n\t");
        builder.append(this.requirements.toString()
            .replace("\n", "\n\t"));
        builder.append("\nProvisions:\n\t");
        builder.append(this.provisions.toString()
            .replace("\n", "\n\t"));

        return builder.toString();
    }
}
