package org.palladiosimulator.somox.analyzer.rules.test.integration;

import org.palladiosimulator.somox.analyzer.rules.impl.JaxRSRules;

public class JaxRsTest extends RuleEngineTest {

    protected JaxRsTest() {
        super("JaxRsProject", new JaxRSRules());
    }

    @Override
    void testRuleEngineRepository() {
        assertComponentExists("jax_rs_AConverter");
    }
}
