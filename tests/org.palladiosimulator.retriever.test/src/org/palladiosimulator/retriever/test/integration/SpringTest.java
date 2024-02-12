package org.palladiosimulator.retriever.test.integration;

import org.palladiosimulator.retriever.extraction.rules.SpringRules;

public class SpringTest extends CaseStudyTest {

    protected SpringTest() {
        super("SpringProject", new SpringRules());
    }

    @Override
    void testRepository() {
        this.assertComponentExists("spring_AController");
    }
}
