package org.palladiosimulator.somox.analyzer.rules.engine;

import java.util.List;

import org.palladiosimulator.somox.analyzer.rules.blackboard.CompilationUnitWrapper;

public interface IPCMDetector {
    List<CompilationUnitWrapper> getWrappedComponents();
}
