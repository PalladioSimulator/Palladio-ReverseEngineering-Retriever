package org.palladiosimulator.somox.analyzer.rules.engine.test;

import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;

public class JaxRsTest extends RuleEngineTest {

	protected JaxRsTest() {
		super("JaxRsProject", DefaultRule.JAX_RS);
	}

	/**
	 * Tests the basic functionality of the RuleEngineAnalyzer when executing the
	 * JAX_RS rule. Requires it to execute without an exception and produce an
	 * output file with the correct contents.
	 */
	@Override
	@Test
	void test() {
		assertTrue(containsComponent("jax_rs_AConverter"));
	}
}
