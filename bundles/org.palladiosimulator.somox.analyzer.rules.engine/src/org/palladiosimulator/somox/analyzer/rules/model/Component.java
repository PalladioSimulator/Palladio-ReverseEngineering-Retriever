package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.Objects;

import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * Components are {@code CompilationUnits}. They provide and require interfaces.
 * 
 * @see CompilationUnit
 * @author Florian Bossert
 */
public class Component {
    private final CompilationUnit compilationUnit;
    private final Requirements requirements;
    private final Provisions provisions;

    public Component(CompilationUnit compilationUnit, Requirements requirements, Provisions provisions) {
        this.compilationUnit = compilationUnit;
        this.requirements = requirements;
        this.provisions = provisions;
    }

    public Requirements requirements() {
        return requirements;
    }

    public Provisions provisions() {
        return provisions;
    }

    public CompilationUnit compilationUnit() {
        return compilationUnit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(compilationUnit, provisions, requirements);
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
        return Objects.equals(compilationUnit, other.compilationUnit) && Objects.equals(provisions, other.provisions)
                && Objects.equals(requirements, other.requirements);
    }
}
