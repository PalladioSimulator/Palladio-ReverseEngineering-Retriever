package org.palladiosimulator.somox.analyzer.rules.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;

public class SpringTest extends RuleEngineTest {

    protected SpringTest() {
        super("SpringProject", DefaultRule.SPRING, DefaultRule.SPRING_EMFTEXT);
    }

    /**
     * Tests the basic functionality of the RuleEngineAnalyzer when executing the SPRING rule.
     * Requires it to execute without an exception and produce an output file with the correct
     * contents.
     */
    @Override
    @ParameterizedTest
    @MethodSource("discovererProvider")
    void test(boolean emfText) {
        assertTrue(containsComponent("spring_AComponent", emfText));
    }
}
