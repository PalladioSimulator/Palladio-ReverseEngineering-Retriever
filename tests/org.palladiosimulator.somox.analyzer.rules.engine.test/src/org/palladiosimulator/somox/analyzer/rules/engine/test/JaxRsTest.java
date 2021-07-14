package org.palladiosimulator.somox.analyzer.rules.engine.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;

public class JaxRsTest extends RuleEngineTest {

    protected JaxRsTest() {
        super("SpringProject", DefaultRule.JAX_RS);
    }

    /**
     * Tests the basic functionality of the RuleEngineAnalyzer when executing the SPRING rule.
     * Requires it to execute without an exception and produce an output file with the correct contents.
     */
    @Disabled("Generated repository produces errors when validated!")
    void test() {
        assertTrue(3 <= getComponents().size());
        assertTrue(1 <= getDatatypes().size());
        assertEquals(0, getFailuretypes().size());
        assertTrue(2 <= getInterfaces().size());
        
        assertTrue(getComponents().stream().anyMatch(x -> x.getEntityName() == "jax_rs_AConverter"));
    }
}
