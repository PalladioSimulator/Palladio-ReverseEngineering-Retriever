package org.palladiosimulator.somox.analyzer.rules.test.integration;

import org.junit.jupiter.api.Test;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;

public class PetclinicTest extends RuleEngineTest {

    protected PetclinicTest() {
        super("external/spring-petclinic-microservices-2.3.6", DefaultRule.SPRING);
    }

    /**
     * Tests the basic functionality of the RuleEngineAnalyzer when executing the SPRING rule.
     * Requires it to execute without an exception and produce an output file with the correct
     * contents.
     */
    @Override
    @Test
    void test() {
        assertComponentExists("org_springframework_samples_petclinic_api_boundary_web_ApiGatewayController");
        assertMaxParameterCount(2, "/owners", "");
        assertMaxParameterCount(1, "/api/gateway/owners", "");
        assertComponentRequiresComponent("org_springframework_samples_petclinic_customers_web_PetResource",
                "org_springframework_samples_petclinic_customers_model_PetRepository");
        assertComponentRequiresComponent("org_springframework_samples_petclinic_customers_web_PetResource",
                "org_springframework_samples_petclinic_customers_model_OwnerRepository");
    }
}
