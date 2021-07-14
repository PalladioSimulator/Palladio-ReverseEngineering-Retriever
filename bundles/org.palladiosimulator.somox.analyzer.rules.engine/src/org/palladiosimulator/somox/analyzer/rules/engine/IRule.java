package org.palladiosimulator.somox.analyzer.rules.engine;

import org.emftext.language.java.containers.impl.CompilationUnitImpl;

/**
* This interface has to be implemented in order to write rules.
* The method will be used by the RuleEngine class to process all written rule lines which are inside the method.
*/
public abstract class IRule {
    
    protected PCMDetectorSimple pcmDetector;
    
    public IRule(PCMDetectorSimple pcmDetector) {
        this.pcmDetector = pcmDetector;
    }
    
    public abstract boolean processRules(CompilationUnitImpl unitImpl);
}