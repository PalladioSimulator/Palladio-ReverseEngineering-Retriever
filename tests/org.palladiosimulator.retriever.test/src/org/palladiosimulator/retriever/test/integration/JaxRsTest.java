package org.palladiosimulator.retriever.test.integration;

import org.palladiosimulator.retriever.extraction.rules.JaxRSRules;

public class JaxRsTest extends RuleEngineTest {

    protected JaxRsTest() {
        super("JaxRsProject", new JaxRSRules());
    }

    @Override
    void testRuleEngineRepository() {
        assertComponentExists("jax_rs_AWebService");
    }
}
