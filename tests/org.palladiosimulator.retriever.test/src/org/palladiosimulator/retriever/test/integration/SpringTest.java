package org.palladiosimulator.retriever.test.integration;

import org.palladiosimulator.retriever.extraction.rules.SpringRules;

public class SpringTest extends RuleEngineTest {

    protected SpringTest() {
        super("SpringProject", new SpringRules());
    }

    @Override
    void testRuleEngineRepository() {
        assertComponentExists("spring_AController");
    }
}
