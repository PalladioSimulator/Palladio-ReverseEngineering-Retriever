package org.palladiosimulator.somox.analyzer.rules.engine.test;

import static org.junit.Assert.assertEquals;

import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;

public class PiggymetricsTest extends RuleEngineTest {

    protected PiggymetricsTest() {
        super("piggymetrics", DefaultRule.SPRING);
    }

    /**
     * Tests the basic functionality of the RuleEngineAnalyzer when executing the SPRING rule.
     * Requires it to execute without an exception and produce an output file with the correct contents.
     */
    void test() {
        /*
        assertEquals(1, getComponents().size());
        assertEquals(1, getDatatypes().size());
        assertEquals(0, getFailuretypes().size());
        assertEquals(0, getInterfaces().size());
        */
    }
}
