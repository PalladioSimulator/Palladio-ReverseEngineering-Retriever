package org.palladiosimulator.somox.analyzer.rules.test.integration;

import org.junit.jupiter.api.Test;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;

public class SpringTest extends RuleEngineTest {

    protected SpringTest() {
        super("SpringProject", DefaultRule.SPRING);
    }

    /**
     * Tests the basic functionality of the RuleEngineAnalyzer when executing the SPRING rule.
     * Requires it to execute without an exception and produce an output file with the correct
     * contents.
     */
    @Override
    @Test
    void test() {
        assertComponentExists("spring_AController");
    }
}
