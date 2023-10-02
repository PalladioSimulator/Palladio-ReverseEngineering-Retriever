package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Components are {@code CompilationUnits}. They provide and require interfaces.
 * 
 * @see CompilationUnit
 * @author Florian Bossert
 */
public class Component {
    private final Optional<CompilationUnit> compilationUnit;
    private final String name;
    private final Requirements requirements;
    private final Provisions provisions;

    public Component(String name, Requirements requirements, Provisions provisions) {
        this(Optional.empty(), name, requirements, provisions);
    }

    public Component(CompilationUnit compilationUnit, Requirements requirements, Provisions provisions) {
        this(Optional.of(compilationUnit), toName(compilationUnit), requirements, provisions);
    }

    public Component(Optional<CompilationUnit> compilationUnit, String name, Requirements requirements,
            Provisions provisions) {
        this.compilationUnit = compilationUnit;
        this.name = name;
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
        return compilationUnit;
    }

    public String name() {
        return name;
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

    private static String toName(CompilationUnit compilationUnit) {
        @SuppressWarnings("unchecked")
        List<AbstractTypeDeclaration> types = (List<AbstractTypeDeclaration>) compilationUnit.types();
        if (types.isEmpty()) {
            return "void";
        }
        AbstractTypeDeclaration firstTypeDecl = types.get(0);
        ITypeBinding binding = firstTypeDecl.resolveBinding();
        if (binding == null) {
            return firstTypeDecl.getName()
                .getFullyQualifiedName();
        }
        return binding.getQualifiedName();
    }
}
