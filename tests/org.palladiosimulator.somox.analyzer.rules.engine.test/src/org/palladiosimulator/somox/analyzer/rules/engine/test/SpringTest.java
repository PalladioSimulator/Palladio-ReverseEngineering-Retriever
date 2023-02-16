package org.palladiosimulator.somox.analyzer.rules.engine.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;

public class SpringTest extends RuleEngineTest {

	protected SpringTest() {
		super("SpringProject", DefaultRule.SPRING, DefaultRule.SPRING_EMFTEXT);
	}

	/**
	 * Tests the basic functionality of the RuleEngineAnalyzer when executing the
	 * SPRING rule. Requires it to execute without an exception and produce an
	 * output file with the correct contents.
	 */
	@Override
	@Test
	void test() {
		assertTrue(containsComponent("spring_AComponent"));
	}
}
