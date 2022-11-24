package org.palladiosimulator.somox.analyzer.rules.blackboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
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
        eclipseCompUnit = null;
    }

    public CompilationUnitWrapper(CompilationUnit eclipseCompUnit) {
        emftextCompUnit = null;
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

    public String getName() {
        if (isEMFTextCompilationUnit()) {
            return emftextCompUnit.getName();
        }
        // TODO is this actually only the name?
        return ((AbstractTypeDeclaration) eclipseCompUnit.types()
            .get(0)).getName()
                .getIdentifier();
    }

    @Override
    public int hashCode() {
        return Objects.hash(eclipseCompUnit, emftextCompUnit);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        CompilationUnitWrapper other = (CompilationUnitWrapper) obj;
        return Objects.equals(eclipseCompUnit, other.eclipseCompUnit)
                && Objects.equals(emftextCompUnit, other.emftextCompUnit);
    }

    public static <T> List<CompilationUnitWrapper> wrap(Collection<T> compUnits) {
        if (compUnits != null) {
            if (compUnits.stream()
                .anyMatch(CompilationUnitImpl.class::isInstance)) {
                return compUnits.stream()
                    .map(CompilationUnitImpl.class::cast)
                    .map(CompilationUnitWrapper::new)
                    .collect(Collectors.toCollection(ArrayList::new));
            }
            if (compUnits.stream()
                .anyMatch(CompilationUnit.class::isInstance)) {
                return compUnits.stream()
                    .map(CompilationUnit.class::cast)
                    .map(CompilationUnitWrapper::new)
                    .collect(Collectors.toCollection(ArrayList::new));
            }
        }
        return new ArrayList<>();
    }
}
