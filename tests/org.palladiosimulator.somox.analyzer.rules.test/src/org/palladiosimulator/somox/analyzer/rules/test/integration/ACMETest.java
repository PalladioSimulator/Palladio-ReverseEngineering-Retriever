package org.palladiosimulator.somox.analyzer.rules.test.integration;

import org.palladiosimulator.somox.analyzer.rules.impl.JaxRSRules;

public class ACMETest extends RuleEngineTest {

    protected ACMETest() {
        super("external/acmeair-1.2.0", new JaxRSRules());
    }

    @Override
    void testRuleEngineRepository() {
        assertComponentExists("com_acmeair_entities_Flight");
        assertComponentExists("com_acmeair_wxs_service_FlightServiceImpl");

        assertComponentProvidesOperation("com_acmeair_morphia_services_CustomerServiceImpl",
                "com_acmeair_service_CustomerService", "getCustomerByUsername");
        assertComponentProvidesOperation("com_acmeair_wxs_service_FlightServiceImpl",
                "com_acmeair_service_FlightService", "getFlightByAirports");
        assertComponentProvidesOperation("com_acmeair_morphia_services_BookingServiceImpl",
                "com_acmeair_service_BookingService", "bookFlight");

        assertMaxParameterCount(2, "com_acmeair_service_BookingService", "bookFlight");

        assertComponentRequiresComponent("com_acmeair_morphia_services_BookingServiceImpl",
                "com_acmeair_morphia_services_CustomerServiceImpl");
        assertComponentRequiresComponent("com_acmeair_morphia_services_CustomerServiceImpl",
                "com_acmeair_morphia_services_DefaultKeyGeneratorImpl");
    }
}
