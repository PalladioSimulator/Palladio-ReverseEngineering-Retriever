package org.palladiosimulator.somox.analyzer.rules.engine;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.core.dom.CompilationUnit;

public class Composite {

    private String name;
    private Set<CompilationUnit> parts;
    private Set<String> internalInterfaces;
    private Set<String> requiredInterfaces;
    private Set<String> providedInterfaces;
    
    public Composite(String name, Set<CompilationUnit> parts, Set<String> requiredInterfaces, Set<String> providedInterfaces, Set<String> internalInterfaces) {
        this.name = name;
        this.parts = parts;
        this.internalInterfaces = internalInterfaces;
        this.requiredInterfaces = requiredInterfaces;
        this.providedInterfaces = providedInterfaces;
    }
    
    public String getName() {
        return name;
    }

    public Set<CompilationUnit> getParts() {
        return Collections.unmodifiableSet(parts);
    }

    public Set<String> getInternalInterfaces() {
        return Collections.unmodifiableSet(internalInterfaces);
    }

    public Set<String> getRequiredInterfaces() {
        return Collections.unmodifiableSet(requiredInterfaces);
    }

    public Set<String> getProvidedInterfaces() {
        return Collections.unmodifiableSet(providedInterfaces);
    }
}
