package org.palladiosimulator.retriever.test.integration;

import org.palladiosimulator.retriever.extraction.rules.SpringRules;

public class PiggymetricsTest extends CaseStudyTest {

    protected PiggymetricsTest() {
        super("external/piggymetrics-spring.version.2.0.3", new SpringRules());
    }

    @Override
    void testRepository() {
        this.assertComponentExists("com_piggymetrics_account_client_AuthServiceClient");
        this.assertComponentExists("com_piggymetrics_notification_service_NotificationServiceImpl");

        this.assertComponentProvidesOperation("com_piggymetrics_statistics_controller_StatisticsController",
                "statistics-service/statistics", "statistics-service/statistics/current");
        this.assertComponentProvidesOperation("com_piggymetrics_account_controller_AccountController", "account-service/accounts",
                "account-service/accounts");
        this.assertComponentProvidesOperation("com_piggymetrics_notification_controller_RecipientController",
                "notification-service/notifications/recipients", "notification-service/notifications/recipients");

        this.assertMaxParameterCount(2, "com_piggymetrics_notification_service_RecipientService", "markNotified");

        this.assertComponentRequiresComponent("com_piggymetrics_notification_client_AccountServiceClient",
                "com_piggymetrics_account_controller_AccountController");

        this.assertComponentRequiresComponent("com_piggymetrics_auth_service_UserServiceImpl",
                "com_piggymetrics_auth_repository_UserRepository");
        this.assertComponentRequiresComponent("com_piggymetrics_notification_controller_RecipientController",
                "com_piggymetrics_notification_service_RecipientServiceImpl");

        this.assertInSameCompositeComponent("com_piggymetrics_notification_controller_RecipientController",
                "com_piggymetrics_notification_service_NotificationServiceImpl");
        this.assertInSameCompositeComponent("com_piggymetrics_notification_controller_RecipientController",
                "com_piggymetrics_notification_service_RecipientServiceImpl");
        this.assertInSameCompositeComponent("com_piggymetrics_notification_service_NotificationServiceImpl",
                "com_piggymetrics_notification_client_AccountServiceClient");
        this.assertInSameCompositeComponent("com_piggymetrics_account_controller_AccountController",
                "com_piggymetrics_account_client_AuthServiceClient");
        this.assertInSameCompositeComponent("com_piggymetrics_account_controller_AccountController",
                "com_piggymetrics_account_client_StatisticsServiceClient");
    }
}
