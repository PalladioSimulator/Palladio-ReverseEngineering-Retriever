package org.palladiosimulator.retriever.test.integration;

import org.palladiosimulator.retriever.extraction.rules.JaxRSRules;

public class JaxRsTest extends CaseStudyTest {

    protected JaxRsTest() {
        super("JaxRsProject", new JaxRSRules());
    }

    @Override
    void testRepository() {
        this.assertComponentExists("jax_rs_AWebService");
    }
}
