package org.palladiosimulator.somox.analyzer.rules.blackboard;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.emftext.language.java.containers.impl.CompilationUnitImpl;

/**
 * A wrapper for both the EMFText {@code CompilationUnit[Impl]} and the Eclipse parser
 * {@code CompilationUnit}. It only contains one of the two variants per instance.
 * 
 * @author Florian Bossert
 *
 * @see org.emftext.language.java.containers.impl.CompilationUnitImpl
 * @see org.eclipse.jdt.core.dom.CompilationUnit
 */
public class CompilationUnitWrapper {
    private final CompilationUnitImpl emftextCompUnit;
    private final CompilationUnit eclipseCompUnit;

    public CompilationUnitWrapper(CompilationUnitImpl emftextCompUnit) {
        this.emftextCompUnit = emftextCompUnit;
        this.eclipseCompUnit = null;
    }

    public CompilationUnitWrapper(CompilationUnit eclipseCompUnit) {
        this.emftextCompUnit = null;
        this.eclipseCompUnit = eclipseCompUnit;
    }
    
    boolean isEclipseCompilationUnit() {
        return eclipseCompUnit != null;
    }
    
    boolean isEMFTextCompilationUnit() {
        return emftextCompUnit != null;
    }
}
