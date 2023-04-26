package org.palladiosimulator.somox.analyzer.rules.test.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;

public class ACMETest extends RuleEngineTest {

	protected ACMETest() {
		super("external/acmeair-1.2.0", DefaultRule.JAX_RS);
	}

	/**
	 * Tests the basic functionality of the RuleEngineAnalyzer when executing the
	 * JAX_RS rule. Requires it to execute without an exception and produce an
	 * output file with the correct contents.
	 */
	@Override
	@Test
	void test() {
		assertTrue(containsComponent("com_acmeair_entities_Flight"));
		assertTrue(containsComponent("com_acmeair_wxs_service_FlightServiceImpl"));
		assertMaxParameterCount(2, "com_acmeair_service_BookingService", "bookFlight");
	}
}
