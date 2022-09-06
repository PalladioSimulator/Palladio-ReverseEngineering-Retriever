package org.palladiosimulator.somox.analyzer.rules.engine;

import java.nio.file.Path;

import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;

/**
 * This interface has to be implemented in order to write rules. The method will be used by the
 * RuleEngine class to process all written rule lines which are inside the method.
 */
public abstract class IRule {

    protected RuleEngineBlackboard blackboard;

    public IRule(RuleEngineBlackboard blackboard) {
        this.blackboard = blackboard;
    }

    public abstract boolean processRules(Path path);
}