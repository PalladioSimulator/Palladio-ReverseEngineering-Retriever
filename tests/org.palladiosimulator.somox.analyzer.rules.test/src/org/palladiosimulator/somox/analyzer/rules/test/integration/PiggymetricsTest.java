package org.palladiosimulator.somox.analyzer.rules.test.integration;

import org.junit.jupiter.api.Test;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;

public class PiggymetricsTest extends RuleEngineTest {

    protected PiggymetricsTest() {
        super("external/piggymetrics-spring.version.2.0.3", DefaultRule.SPRING);
    }

    /**
     * Tests the basic functionality of the RuleEngineAnalyzer when executing the SPRING rule.
     * Requires it to execute without an exception and produce an output file with the correct
     * contents.
     */
    @Override
    @Test
    void test() {
        assertComponentExists("com_piggymetrics_account_client_AuthServiceClient");
        assertComponentExists("com_piggymetrics_notification_service_NotificationServiceImpl");
        assertMaxParameterCount(2, "com_piggymetrics_notification_service_RecipientService", "markNotified");
        assertComponentRequiresComponent("com_piggymetrics_notification_client_AccountServiceClient",
                "com_piggymetrics_account_controller_AccountController");
        assertComponentRequiresComponent("com_piggymetrics_auth_service_UserServiceImpl",
                "com_piggymetrics_auth_repository_UserRepository");
        assertComponentRequiresComponent("com_piggymetrics_notification_controller_RecipientController",
                "com_piggymetrics_notification_service_RecipientServiceImpl");
    }
}
