package org.palladiosimulator.somox.analyzer.rules.model;

import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * Used to build {@code Component}s.
 * 
 * @see Component
 * @author Florian Bossert
 */
public class ComponentBuilder {
    private final CompilationUnit compilationUnit;
    private final RequirementsBuilder requirements;
    private final ProvisionsBuilder provisions;

    public ComponentBuilder(CompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;
        requirements = new RequirementsBuilder();
        this.provisions = new ProvisionsBuilder();
    }

    public RequirementsBuilder requirements() {
        return requirements;
    }

    public ProvisionsBuilder provisions() {
        return provisions;
    }

    public Component create() {
        return new Component(compilationUnit, requirements.create(), provisions.create());
    }
}
