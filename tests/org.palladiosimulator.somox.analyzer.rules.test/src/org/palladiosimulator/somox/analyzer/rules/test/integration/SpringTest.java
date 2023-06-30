package org.palladiosimulator.somox.analyzer.rules.test.integration;

import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;

public class SpringTest extends RuleEngineTest {

    protected SpringTest() {
        super("SpringProject", DefaultRule.SPRING);
    }

    @Override
    void testRuleEngineRepository() {
        assertComponentExists("spring_AController");
    }
}
