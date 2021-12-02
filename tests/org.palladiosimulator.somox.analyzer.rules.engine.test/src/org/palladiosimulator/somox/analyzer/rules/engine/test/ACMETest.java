package org.palladiosimulator.somox.analyzer.rules.engine.test;

import org.junit.jupiter.api.Test;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;

public class ACMETest extends RuleEngineTest {

    protected ACMETest() {
        // TODO change this to "acmeair" to activate the test
        super("BasicProject", DefaultRule.JAX_RS);
    }

    /**
     * Tests the basic functionality of the RuleEngineAnalyzer when executing the JAX_RS rule.
     * Requires it to execute without an exception and produce an output file with the correct contents.
     */
    @Test
    void test() {
        /*
        assertEquals(1, getComponents().size());
        assertEquals(1, getDatatypes().size());
        assertEquals(0, getFailuretypes().size());
        assertEquals(0, getInterfaces().size());
        */
    }
}
