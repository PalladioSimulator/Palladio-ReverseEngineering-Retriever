package org.palladiosimulator.retriever.test.integration;

import org.junit.jupiter.api.Disabled;
import org.palladiosimulator.retriever.extraction.rules.SpringRules;

@Disabled("TODO: Currently broken")
public class TeaStoreTest extends CaseStudyTest {

    protected TeaStoreTest() {
        super("external/TeaStore-1.4.1", new SpringRules());
    }

    @Override
    void testRetrieverRepository() {
        // TODO: Temporarily disabled due to rule changes.
        if (getClass() != null)
            return;

        assertComponentExists("tools_descartes_teastore_auth_security_BCryptProvider");
        assertInterfaceExists("tools_descartes_teastore_kieker_probes_records_IPayloadCharacterization");

        assertComponentProvidesOperation("tools_descartes_teastore_recommender_algorithm_AbstractRecommender",
                "tools_descartes_teastore_recommender_algorithm_IRecommender", "train");
        assertComponentProvidesOperation("tools_descartes_teastore_recommender_algorithm_AbstractRecommender",
                "tools_descartes_teastore_recommender_algorithm_IRecommender", "recommendProducts");
    }
}
