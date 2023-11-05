package org.palladiosimulator.somox.analyzer.rules.test.integration;

import org.palladiosimulator.somox.analyzer.rules.impl.SpringRules;

public class TeaStoreTest extends RuleEngineTest {

    protected TeaStoreTest() {
        super("external/TeaStore-1.4.1", new SpringRules());
    }

    @Override
    void testRuleEngineRepository() {
        assertComponentExists("tools_descartes_teastore_auth_security_BCryptProvider");
        assertInterfaceExists("tools_descartes_teastore_kieker_probes_records_IPayloadCharacterization");

        assertComponentProvidesOperation("tools_descartes_teastore_recommender_algorithm_AbstractRecommender",
                "tools_descartes_teastore_recommender_algorithm_IRecommender", "train");
        assertComponentProvidesOperation("tools_descartes_teastore_recommender_algorithm_AbstractRecommender",
                "tools_descartes_teastore_recommender_algorithm_IRecommender", "recommendProducts");
    }
}
