package org.palladiosimulator.somox.analyzer.rules.main;

/**
 * A general exception for the rule engine.
 *
 * @author Florian Bossert
 */
public class RuleEngineException extends Exception {

    private static final long serialVersionUID = 8438995877350048404L;

    public RuleEngineException(String message) {
        super(message);
    }

    public RuleEngineException(String message, Throwable cause) {
        super(message, cause);
    }
}
