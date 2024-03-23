package org.palladiosimulator.retriever.test.integration;

import org.palladiosimulator.retriever.extraction.rules.JaxRSRules;

public class ACMETest extends CaseStudyTest {

    protected ACMETest() {
        super("external/acmeair-1.2.0", new JaxRSRules());
    }

    @Override
    void testRepository() {
        this.assertComponentExists("com_acmeair_wxs_service_FlightServiceImpl");

        this.assertComponentProvidesOperation("com_acmeair_morphia_services_CustomerServiceImpl",
                "com_acmeair_service_CustomerService", "getCustomerByUsername");
        this.assertComponentProvidesOperation("com_acmeair_wxs_service_FlightServiceImpl",
                "com_acmeair_service_FlightService", "getFlightByAirports");
        this.assertComponentProvidesOperation("com_acmeair_morphia_services_BookingServiceImpl",
                "com_acmeair_service_BookingService", "bookFlight");

        this.assertMaxParameterCount(2, "com_acmeair_service_BookingService", "bookFlight");

        this.assertComponentRequiresComponent("com_acmeair_web_FlightsREST",
                "com_acmeair_morphia_services_FlightServiceImpl");
        this.assertComponentRequiresComponent("com_acmeair_web_LoginREST",
                "com_acmeair_morphia_services_CustomerServiceImpl");
    }
}
