package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
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

    @Override
    public String toString() {
        String name = "null";
        if (compilationUnit != null) {
            @SuppressWarnings("unchecked")
            List<AbstractTypeDeclaration> types = (List<AbstractTypeDeclaration>) compilationUnit.types();
            if (types.isEmpty()) {
                name = "void";
            } else {
                name = types.get(0)
                    .getName()
                    .getFullyQualifiedName();
            }
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Name: ");
        builder.append(name);
        builder.append("\nRequirements:\n\t");
        builder.append(requirements.toString()
            .replace("\n", "\n\t"));
        builder.append("\nProvisions:\n\t");
        builder.append(provisions.toString()
            .replace("\n", "\n\t"));

        return builder.toString();
    }
}
