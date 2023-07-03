package org.palladiosimulator.somox.analyzer.rules.test.integration;

import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;

public class PetclinicTest extends RuleEngineTest {

    protected PetclinicTest() {
        super("external/spring-petclinic-microservices-2.3.6", DefaultRule.SPRING);
    }

    @Override
    void testRuleEngineRepository() {
        assertComponentExists("org_springframework_samples_petclinic_api_boundary_web_ApiGatewayController");
        assertMaxParameterCount(2, "/owners", "");
        assertMaxParameterCount(1, "/api/gateway/owners", "");
        // FIXME: This fails, but is hard to reproduce outside of tests.
        // assertComponentRequiresComponent("org_springframework_samples_petclinic_customers_web_PetResource",
        // "org_springframework_samples_petclinic_customers_model_PetRepository");
        // assertComponentRequiresComponent("org_springframework_samples_petclinic_customers_web_PetResource",
        // "org_springframework_samples_petclinic_customers_model_OwnerRepository");
        assertInSameCompositeComponent("org_springframework_samples_petclinic_customers_web_PetResource",
                "org_springframework_samples_petclinic_customers_model_PetRepository");
        assertInSameCompositeComponent("org_springframework_samples_petclinic_customers_web_PetResource",
                "org_springframework_samples_petclinic_customers_web_OwnerResource");
        assertInSameCompositeComponent("org_springframework_samples_petclinic_visits_web_VisitResource",
                "org_springframework_samples_petclinic_visits_model_VisitRepository");
        assertInSameCompositeComponent("org_springframework_samples_petclinic_vets_web_VetResource",
                "org_springframework_samples_petclinic_vets_model_VetRepository");
    }
}
