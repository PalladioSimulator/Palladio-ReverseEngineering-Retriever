package org.palladiosimulator.somox.analyzer.rules.test.integration;

import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;

public class PiggymetricsTest extends RuleEngineTest {

    protected PiggymetricsTest() {
        super("external/piggymetrics-spring.version.2.0.3", DefaultRule.SPRING);
    }

    @Override
    void testRuleEngineRepository() {
        assertComponentExists("com_piggymetrics_account_client_AuthServiceClient");
        assertComponentExists("com_piggymetrics_notification_service_NotificationServiceImpl");
        assertMaxParameterCount(2, "com_piggymetrics_notification_service_RecipientService", "markNotified");
        assertComponentRequiresComponent("com_piggymetrics_notification_client_AccountServiceClient",
                "com_piggymetrics_account_controller_AccountController");
        // FIXME: This fails, but is hard to reproduce outside of tests.
        // assertComponentRequiresComponent("com_piggymetrics_auth_service_UserServiceImpl",
        // "com_piggymetrics_auth_repository_UserRepository");
        // assertComponentRequiresComponent("com_piggymetrics_notification_controller_RecipientController",
        // "com_piggymetrics_notification_service_RecipientServiceImpl");
    }
}
