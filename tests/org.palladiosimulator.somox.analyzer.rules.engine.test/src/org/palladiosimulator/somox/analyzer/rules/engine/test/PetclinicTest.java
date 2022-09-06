package org.palladiosimulator.somox.analyzer.rules.engine.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;

public class PetclinicTest extends RuleEngineTest {

    protected PetclinicTest() {
        super("external/spring-petclinic-microservices-2.3.6", DefaultRule.SPRING, DefaultRule.SPRING_EMFTEXT);
    }

    /**
     * Tests the basic functionality of the RuleEngineAnalyzer when executing the SPRING rule.
     * Requires it to execute without an exception and produce an output file with the correct
     * contents.
     */
    @ParameterizedTest
    @MethodSource("discovererProvider")
    void test(boolean emfText) {
    	if (emfText) return;
        assertTrue(containsComponent("org_springframework_samples_petclinic_api_application_VisitsServiceClient", emfText));
        assertMaxParameterCount(1, "org_springframework_samples_petclinic_customers_model_PetRepository",
                "findPetTypeById", emfText);
        assertMaxParameterCount(0, "org_springframework_samples_petclinic_customers_model_PetRepository",
                "findPetTypes", emfText);
    }
}
