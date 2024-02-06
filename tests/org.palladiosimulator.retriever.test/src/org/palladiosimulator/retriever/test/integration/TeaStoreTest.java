package org.palladiosimulator.retriever.test.integration;

import org.palladiosimulator.retriever.extraction.rules.SpringRules;

public class TeaStoreTest extends CaseStudyTest {

    protected TeaStoreTest() {
        super("external/TeaStore-1.4.1", new SpringRules());
    }

    @Override
    void testRetrieverRepository() {
        this.assertComponentExists("tools_descartes_teastore_auth_security_BCryptProvider");
        this.assertInterfaceExists("tools_descartes_teastore_kieker_probes_records_IPayloadCharacterization");

        this.assertComponentProvidesOperation("tools_descartes_teastore_recommender_algorithm_AbstractRecommender",
                "tools_descartes_teastore_recommender_algorithm_IRecommender", "train");
        this.assertComponentProvidesOperation("tools_descartes_teastore_recommender_algorithm_AbstractRecommender",
                "tools_descartes_teastore_recommender_algorithm_IRecommender", "recommendProducts");
    }
}
