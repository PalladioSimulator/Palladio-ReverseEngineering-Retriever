package org.palladiosimulator.somox.analyzer.rules.test.integration;

import org.palladiosimulator.somox.analyzer.rules.impl.SpringRules;

public class SpringTest extends RuleEngineTest {

    protected SpringTest() {
        super("SpringProject", new SpringRules());
    }

    @Override
    void testRuleEngineRepository() {
        assertComponentExists("spring_AController");
    }
}
