package org.palladiosimulator.somox.analyzer.rules.test.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;

public class TeaStoreTest extends RuleEngineTest {

	protected TeaStoreTest() {
		super("external/TeaStore-1.4.1", DefaultRule.JAX_RS);
	}

	/**
	 * Tests the basic functionality of the RuleEngineAnalyzer when executing the
	 * JAX_RS rule. Requires it to execute without an exception and produce an
	 * output file with the correct contents.
	 */
	@Override
	@Test
	void test() {
		assertTrue(containsComponent("tools_descartes_teastore_auth_security_BCryptProvider"));
		assertTrue(
				containsOperationInterface("tools_descartes_teastore_kieker_probes_records_IPayloadCharacterization"));
	}
}
