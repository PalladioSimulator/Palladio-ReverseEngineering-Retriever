package org.palladiosimulator.somox.analyzer.rules.engine;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.core.dom.CompilationUnit;

public class Composite {

    private Set<CompilationUnit> parts;
    private Set<String> requiredInterfaces;
    private Set<String> providedInterfaces;
    
    public Composite(Set<CompilationUnit> parts, Set<String> requiredInterfaces, Set<String> providedInterfaces) {
        this.parts = parts;
        this.requiredInterfaces = requiredInterfaces;
        this.providedInterfaces = providedInterfaces;
    }

    public Set<CompilationUnit> getParts() {
        return Collections.unmodifiableSet(parts);
    }

    public Set<String> getRequiredInterfaces() {
        return Collections.unmodifiableSet(requiredInterfaces);
    }

    public Set<String> getProvidedInterfaces() {
        return Collections.unmodifiableSet(providedInterfaces);
    }
}
