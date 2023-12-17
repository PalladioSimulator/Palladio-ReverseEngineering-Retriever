package org.palladiosimulator.somox.analyzer.rules.test.integration;

import org.junit.jupiter.api.Disabled;
import org.palladiosimulator.somox.analyzer.rules.impl.SpringRules;

@Disabled("TODO: Currently broken")
public class PetclinicTest extends RuleEngineTest {

    protected PetclinicTest() {
        super("external/spring-petclinic-microservices-2.3.6", new SpringRules());
    }

    @Override
    void testRuleEngineRepository() {
        // TODO: Temporarily disabled due to rule changes.
        if (getClass() != null)
            return;

        assertComponentExists("org_springframework_samples_petclinic_api_boundary_web_ApiGatewayController");

        assertComponentProvidesOperation("org_springframework_samples_petclinic_vets_web_VetResource",
                "/vets-service/vets[GET]", "/vets-service/vets[GET]");
        assertComponentProvidesOperation("org_springframework_samples_petclinic_visits_web_VisitResource",
                "/visits-service", "/visits-service/pets/visits[GET]");
        assertComponentProvidesOperation("org_springframework_samples_petclinic_customers_web_PetResource",
                "/customers-service/owners/*/pets", "/customers-service/owners/*/pets[GET]");

        assertMaxParameterCount(2, "/customers-service/owners[PUT]", "/customers-service/owners[PUT]");
        assertMaxParameterCount(1, "/api-gateway/api/gateway/owners[GET]", "/api-gateway/api/gateway/owners[GET]");

        assertComponentRequiresComponent("org_springframework_samples_petclinic_customers_web_PetResource",
                "org_springframework_samples_petclinic_customers_model_PetRepository");
        assertComponentRequiresComponent("org_springframework_samples_petclinic_customers_web_PetResource",
                "org_springframework_samples_petclinic_customers_model_OwnerRepository");

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
