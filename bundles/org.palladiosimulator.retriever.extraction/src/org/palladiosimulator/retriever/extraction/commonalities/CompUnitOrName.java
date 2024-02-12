package org.palladiosimulator.retriever.extraction.commonalities;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class CompUnitOrName {

    private final Optional<CompilationUnit> compilationUnit;
    private final String name;

    public CompUnitOrName(final String name) {
        this.compilationUnit = Optional.empty();
        this.name = name;
    }

    public CompUnitOrName(final CompilationUnit compilationUnit) {
        this.compilationUnit = Optional.of(compilationUnit);
        this.name = toName(compilationUnit);
    }

    public boolean isUnit() {
        return this.compilationUnit.isPresent();
    }

    public Optional<CompilationUnit> compilationUnit() {
        return this.compilationUnit;
    }

    public String name() {
        return this.name;
    }

    private static String toName(final CompilationUnit compilationUnit) {
        @SuppressWarnings("unchecked")
        final List<AbstractTypeDeclaration> types = compilationUnit.types();
        if (types.isEmpty()) {
            return "void";
        }
        final AbstractTypeDeclaration firstTypeDecl = types.get(0);
        final ITypeBinding binding = firstTypeDecl.resolveBinding();
        if (binding == null) {
            return firstTypeDecl.getName()
                .getFullyQualifiedName();
        }
        return binding.getQualifiedName();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.compilationUnit, this.name);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (this.getClass() != obj.getClass())) {
            return false;
        }
        final CompUnitOrName other = (CompUnitOrName) obj;
        return Objects.equals(this.compilationUnit, other.compilationUnit) && Objects.equals(this.name, other.name);
    }
    
    @Override
    public String toString() {
        return this.name();
    }
}
