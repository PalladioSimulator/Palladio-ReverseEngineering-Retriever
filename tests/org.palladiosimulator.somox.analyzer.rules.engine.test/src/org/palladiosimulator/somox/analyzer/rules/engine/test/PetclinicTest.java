package org.palladiosimulator.somox.analyzer.rules.engine.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;

public class PetclinicTest extends RuleEngineTest {

    protected PetclinicTest() {
        super("external/spring-petclinic-microservices-2.3.6", DefaultRule.SPRING_EMFTEXT);
    }

    /**
     * Tests the basic functionality of the RuleEngineAnalyzer when executing the SPRING rule.
     * Requires it to execute without an exception and produce an output file with the correct
     * contents.
     */
    @Test
    void test() {
        assertTrue(containsComponent("org_springframework_samples_petclinic_api_application_VisitsServiceClient"));
        assertMaxParameterCount(1, "org_springframework_samples_petclinic_customers_model_PetRepository", "findPetTypeById");
        assertMaxParameterCount(0, "org_springframework_samples_petclinic_customers_model_PetRepository", "findPetTypes");
    }
}
