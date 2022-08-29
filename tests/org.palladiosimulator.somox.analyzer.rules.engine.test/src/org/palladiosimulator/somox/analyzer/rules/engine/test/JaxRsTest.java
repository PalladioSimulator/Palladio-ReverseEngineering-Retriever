package org.palladiosimulator.somox.analyzer.rules.engine.test;

import static org.junit.Assert.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;

public class JaxRsTest extends RuleEngineTest {

    protected JaxRsTest() {
        super("JaxRsProject", DefaultRule.JAX_RS, DefaultRule.JAX_RS_EMFTEXT);
    }

    /**
     * Tests the basic functionality of the RuleEngineAnalyzer when executing the JAX_RS rule.
     * Requires it to execute without an exception and produce an output file with the correct
     * contents.
     */
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void test(boolean emfText) {
        assertTrue(containsComponent("jax_rs_AConverter", emfText));
    }
}
