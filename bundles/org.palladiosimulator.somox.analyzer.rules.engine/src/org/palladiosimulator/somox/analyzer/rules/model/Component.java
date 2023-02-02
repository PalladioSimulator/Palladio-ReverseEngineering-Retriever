package org.palladiosimulator.somox.analyzer.rules.model;

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
}
