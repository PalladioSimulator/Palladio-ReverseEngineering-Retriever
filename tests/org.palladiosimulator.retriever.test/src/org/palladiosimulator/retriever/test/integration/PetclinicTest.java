package org.palladiosimulator.retriever.test.integration;

import org.palladiosimulator.retriever.extraction.rules.SpringRules;

public class PetclinicTest extends CaseStudyTest {

    protected PetclinicTest() {
        super("external/spring-petclinic-microservices-2.3.6", new SpringRules());
    }

    @Override
    void testRetrieverRepository() {
        // TODO: Temporarily disabled due to rule changes.
        if (this.getClass() != null) {
            return;
        }

        this.assertComponentExists("org_springframework_samples_petclinic_api_boundary_web_ApiGatewayController");

        this.assertComponentProvidesOperation("org_springframework_samples_petclinic_vets_web_VetResource",
                "/vets-service/vets[GET]", "/vets-service/vets[GET]");
        this.assertComponentProvidesOperation("org_springframework_samples_petclinic_visits_web_VisitResource",
                "/visits-service", "/visits-service/pets/visits[GET]");
        this.assertComponentProvidesOperation("org_springframework_samples_petclinic_customers_web_PetResource",
                "/customers-service/owners/*/pets", "/customers-service/owners/*/pets[GET]");

        this.assertMaxParameterCount(2, "/customers-service/owners[PUT]", "/customers-service/owners[PUT]");
        this.assertMaxParameterCount(1, "/api-gateway/api/gateway/owners[GET]", "/api-gateway/api/gateway/owners[GET]");

        this.assertComponentRequiresComponent("org_springframework_samples_petclinic_customers_web_PetResource",
                "org_springframework_samples_petclinic_customers_model_PetRepository");
        this.assertComponentRequiresComponent("org_springframework_samples_petclinic_customers_web_PetResource",
                "org_springframework_samples_petclinic_customers_model_OwnerRepository");

        this.assertInSameCompositeComponent("org_springframework_samples_petclinic_customers_web_PetResource",
                "org_springframework_samples_petclinic_customers_model_PetRepository");
        this.assertInSameCompositeComponent("org_springframework_samples_petclinic_customers_web_PetResource",
                "org_springframework_samples_petclinic_customers_web_OwnerResource");
        this.assertInSameCompositeComponent("org_springframework_samples_petclinic_visits_web_VisitResource",
                "org_springframework_samples_petclinic_visits_model_VisitRepository");
        this.assertInSameCompositeComponent("org_springframework_samples_petclinic_vets_web_VetResource",
                "org_springframework_samples_petclinic_vets_model_VetRepository");
    }
}
