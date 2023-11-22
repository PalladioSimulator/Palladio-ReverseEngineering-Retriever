package org.palladiosimulator.somox.analyzer.rules.model;

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

    public Component(CompUnitOrName compUnitOrName, Requirements requirements, Provisions provisions) {
        this.compUnitOrName = compUnitOrName;
        this.requirements = requirements;
        this.provisions = provisions;
    }

    public Requirements requirements() {
        return requirements;
    }

    public Provisions provisions() {
        return provisions;
    }

    public Optional<CompilationUnit> compilationUnit() {
        return compUnitOrName.compilationUnit();
    }

    public String name() {
        return compUnitOrName.name();
    }
    
    public CompUnitOrName identifier() {
    	return compUnitOrName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(compUnitOrName, provisions, requirements);
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
        Component other = (Component) obj;
        return Objects.equals(compUnitOrName, other.compUnitOrName) && Objects.equals(provisions, other.provisions)
                && Objects.equals(requirements, other.requirements);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Name: ");
        builder.append(name());
        builder.append("\nRequirements:\n\t");
        builder.append(requirements.toString()
            .replace("\n", "\n\t"));
        builder.append("\nProvisions:\n\t");
        builder.append(provisions.toString()
            .replace("\n", "\n\t"));

        return builder.toString();
    }
}
