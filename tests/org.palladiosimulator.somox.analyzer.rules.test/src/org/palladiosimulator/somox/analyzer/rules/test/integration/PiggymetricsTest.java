package org.palladiosimulator.somox.analyzer.rules.test.integration;

import org.palladiosimulator.somox.analyzer.rules.impl.SpringRules;

public class PiggymetricsTest extends RuleEngineTest {

    protected PiggymetricsTest() {
        super("external/piggymetrics-spring.version.2.0.3", new SpringRules());
    }

    @Override
    void testRuleEngineRepository() {
        assertComponentExists("com_piggymetrics_account_client_AuthServiceClient");
        assertComponentExists("com_piggymetrics_notification_service_NotificationServiceImpl");

        assertComponentProvidesOperation("com_piggymetrics_statistics_controller_StatisticsController", "/statistics",
                "/statistics/current");
        assertComponentProvidesOperation("com_piggymetrics_account_controller_AccountController", "/accounts",
                "/accounts");
        assertComponentProvidesOperation("com_piggymetrics_notification_controller_RecipientController",
                "/notifications/recipients", "/notifications/recipients");

        assertMaxParameterCount(2, "com_piggymetrics_notification_service_RecipientService", "markNotified");

        assertComponentRequiresComponent("com_piggymetrics_notification_client_AccountServiceClient",
                "com_piggymetrics_account_controller_AccountController");

        assertComponentRequiresComponent("com_piggymetrics_auth_service_UserServiceImpl",
                "com_piggymetrics_auth_repository_UserRepository");
        assertComponentRequiresComponent("com_piggymetrics_notification_controller_RecipientController",
                "com_piggymetrics_notification_service_RecipientServiceImpl");

        assertInSameCompositeComponent("com_piggymetrics_notification_controller_RecipientController",
                "com_piggymetrics_notification_service_NotificationServiceImpl");
        assertInSameCompositeComponent("com_piggymetrics_notification_controller_RecipientController",
                "com_piggymetrics_notification_service_RecipientServiceImpl");
        assertInSameCompositeComponent("com_piggymetrics_notification_service_NotificationServiceImpl",
                "com_piggymetrics_notification_client_AccountServiceClient");
        assertInSameCompositeComponent("com_piggymetrics_account_controller_AccountController",
                "com_piggymetrics_account_client_AuthServiceClient");
        assertInSameCompositeComponent("com_piggymetrics_account_controller_AccountController",
                "com_piggymetrics_account_client_StatisticsServiceClient");
    }
}
