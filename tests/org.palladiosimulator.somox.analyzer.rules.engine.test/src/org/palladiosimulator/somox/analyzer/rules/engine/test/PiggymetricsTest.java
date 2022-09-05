package org.palladiosimulator.somox.analyzer.rules.engine.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;

public class PiggymetricsTest extends RuleEngineTest {

    protected PiggymetricsTest() {
        super("external/piggymetrics-spring.version.2.0.3", DefaultRule.SPRING, DefaultRule.SPRING_EMFTEXT);
    }

    /**
     * Tests the basic functionality of the RuleEngineAnalyzer when executing the SPRING rule.
     * Requires it to execute without an exception and produce an output file with the correct
     * contents.
     */
    @ParameterizedTest
    @MethodSource("discovererProvider")
    void test(boolean emfText) {
        assertTrue(containsComponent("com_piggymetrics_account_client_AuthServiceClient", emfText));
        assertTrue(containsComponent("com_piggymetrics_notification_service_NotificationServiceImpl", emfText));
        assertMaxParameterCount(2, "com_piggymetrics_notification_service_RecipientService", "markNotified", emfText);
    }
}
