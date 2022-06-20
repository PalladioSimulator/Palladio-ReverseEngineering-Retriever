package org.palladiosimulator.somox.analyzer.rules.engine.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;

public class SpringTest extends RuleEngineTest {

    protected SpringTest() {
        super("SpringProject", DefaultRule.SPRING_EMFTEXT);
    }

    /**
     * Tests the basic functionality of the RuleEngineAnalyzer when executing the SPRING rule.
     * Requires it to execute without an exception and produce an output file with the correct
     * contents.
     */
    @Test
    void test() {
        assertEquals(1, getComponents().size());
        assertEquals(0, getDatatypes().size());
        assertEquals(0, getFailuretypes().size());
        assertEquals(0, getInterfaces().size());

        assertTrue(containsComponent("spring_AComponent"));
    }
}
