package org.palladiosimulator.somox.analyzer.rules.main;

/**
 * A general exception for the rule engine.
 *
 * @author Florian Bossert
 */
public class RuleEngineException extends Exception {
    private static final long serialVersionUID = 669017784165629387L;

    public RuleEngineException(String message) {
        super(message);
    }
}
