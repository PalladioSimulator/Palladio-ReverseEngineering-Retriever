package org.palladiosimulator.retriever.test.integration;

import org.palladiosimulator.retriever.extraction.rules.SpringRules;

public class SpringTest extends CaseStudyTest {

    protected SpringTest() {
        super("SpringProject", new SpringRules());
    }

    @Override
    void testRetrieverRepository() {
        this.assertComponentExists("spring_AController");
    }
}
