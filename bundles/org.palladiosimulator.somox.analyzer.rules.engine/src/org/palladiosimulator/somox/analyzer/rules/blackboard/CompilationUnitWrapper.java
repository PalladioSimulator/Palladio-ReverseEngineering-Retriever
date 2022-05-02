package org.palladiosimulator.somox.analyzer.rules.blackboard;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    public boolean isEMFTextCompilationUnit() {
        return emftextCompUnit != null;
    }

    public boolean isEclipseCompilationUnit() {
        return eclipseCompUnit != null;
    }

    public CompilationUnitImpl getEMFTextCompilationUnit() {
        return emftextCompUnit;
    }

    public CompilationUnit getEclipseCompilationUnit() {
        return eclipseCompUnit;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<CompilationUnitWrapper> wrap(List<T> compUnits) {
        if (compUnits == null) {
            return null;
        } else if (compUnits.isEmpty()) {
            return Collections.emptyList();
        } else if (compUnits.get(0) instanceof CompilationUnitImpl) {
            return ((List<CompilationUnitImpl>) compUnits).stream()
                .map(CompilationUnitWrapper::new)
                .collect(Collectors.toList());
        } else if (compUnits.get(0) instanceof CompilationUnit) {
            return ((List<CompilationUnit>) compUnits).stream()
                .map(CompilationUnitWrapper::new)
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
