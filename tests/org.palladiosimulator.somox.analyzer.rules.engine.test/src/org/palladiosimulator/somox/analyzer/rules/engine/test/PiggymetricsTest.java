package org.palladiosimulator.somox.analyzer.rules.engine.test;

import org.junit.jupiter.api.Test;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;

public class PiggymetricsTest extends RuleEngineTest {

    protected PiggymetricsTest() {
        super("external/piggymetrics-master", DefaultRule.SPRING);
    }

    /**
     * Tests the basic functionality of the RuleEngineAnalyzer when executing the SPRING rule.
     * Requires it to execute without an exception and produce an output file with the correct
     * contents.
     */
    @Test
    void test() {
    }
}
