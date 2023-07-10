package org.palladiosimulator.somox.analyzer.rules.test.integration;

import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;

public class JaxRsTest extends RuleEngineTest {

    protected JaxRsTest() {
        super("JaxRsProject", DefaultRule.JAX_RS);
    }

    @Override
    void testRuleEngineRepository() {
        assertComponentExists("jax_rs_AConverter");
    }
}
