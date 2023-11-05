package org.palladiosimulator.somox.analyzer.rules.configuration;

import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;

public final class RuleEngineBlackboardKeys {
    private static final String CONFIG_PREFIX = "org.palladiosimulator.somox.analyzer.rules.configuration.";
    public static final String RULE_ENGINE_BLACKBOARD_KEY_REPOSITORY = RuleEngineBlackboard.KEY_REPOSITORY;
    public static final String RULE_ENGINE_BLACKBOARD_KEY_SEFF_ASSOCIATIONS = RuleEngineBlackboard.KEY_SEFF_ASSOCIATIONS;
    public static final String RULE_ENGINE_AST2SEFF_OUTPUT_REPOSITORY = CONFIG_PREFIX + "ast2seff.output.repository";
    public static final String RULE_ENGINE_MOCORE_OUTPUT_REPOSITORY = CONFIG_PREFIX + "mocore.output.repository";
    public static final String RULE_ENGINE_MOCORE_OUTPUT_SYSTEM = CONFIG_PREFIX + "mocore.output.system";
    public static final String RULE_ENGINE_MOCORE_OUTPUT_ALLOCATION = CONFIG_PREFIX + "mocore.output.allocation";
    public static final String RULE_ENGINE_MOCORE_OUTPUT_RESOURCE_ENVIRONMENT = CONFIG_PREFIX
            + "mocore.output.resource_environment";
    public static final String RULE_LIST_SEPARATOR = ";";

    private RuleEngineBlackboardKeys() throws IllegalAccessException {
        throw new IllegalAccessException();
    }
}
