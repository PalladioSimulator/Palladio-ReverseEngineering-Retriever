package org.palladiosimulator.retriever.test.integration;

import org.palladiosimulator.retriever.extraction.rules.JaxRSRules;

public class ACMETest extends CaseStudyTest {

    protected ACMETest() {
        super("external/acmeair-1.2.0", new JaxRSRules());
    }

    @Override
    void testRetrieverRepository() {
        assertComponentExists("com_acmeair_wxs_service_FlightServiceImpl");

        assertComponentProvidesOperation("com_acmeair_morphia_services_CustomerServiceImpl",
                "com_acmeair_service_CustomerService", "getCustomerByUsername");
        assertComponentProvidesOperation("com_acmeair_wxs_service_FlightServiceImpl",
                "com_acmeair_service_FlightService", "getFlightByAirports");
        assertComponentProvidesOperation("com_acmeair_morphia_services_BookingServiceImpl",
                "com_acmeair_service_BookingService", "bookFlight");

        assertMaxParameterCount(2, "com_acmeair_service_BookingService", "bookFlight");

        assertComponentRequiresComponent("com_acmeair_web_FlightsREST",
                "com_acmeair_morphia_services_FlightServiceImpl");
        assertComponentRequiresComponent("com_acmeair_web_LoginREST",
                "com_acmeair_morphia_services_CustomerServiceImpl");
    }
}
