package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class CompUnitOrName {
    private final Optional<CompilationUnit> compilationUnit;
    private final String name;

    public CompUnitOrName(String name) {
        this.compilationUnit = Optional.empty();
        this.name = name;
    }

    public CompUnitOrName(CompilationUnit compilationUnit) {
        this.compilationUnit = Optional.of(compilationUnit);
        this.name = toName(compilationUnit);
    }

    public boolean isUnit() {
        return compilationUnit.isPresent();
    }

    public Optional<CompilationUnit> compilationUnit() {
        return compilationUnit;
    }

    public String name() {
        return name;
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
