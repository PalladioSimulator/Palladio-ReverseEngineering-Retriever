package org.palladiosimulator.retriever.test.integration;

import org.palladiosimulator.retriever.extraction.rules.SpringRules;

public class SpringTest extends CaseStudyTest {

    protected SpringTest() {
        super("SpringProject", new SpringRules());
    }

    @Override
    void testRetrieverRepository() {
        assertComponentExists("spring_AController");
    }
}
