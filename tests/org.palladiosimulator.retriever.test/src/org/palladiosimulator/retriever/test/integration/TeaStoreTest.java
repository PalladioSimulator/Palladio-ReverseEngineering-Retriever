package org.palladiosimulator.retriever.test.integration;

import org.palladiosimulator.retriever.extraction.rules.JaxRSRules;

public class TeaStoreTest extends CaseStudyTest {

    protected TeaStoreTest() {
        super("external/TeaStore-1.4.1", new JaxRSRules());
    }

    @Override
    void testRepository() {
        this.assertInterfaceExists("SERVICE-HOST/login[GET]");

        this.assertComponentProvidesOperation("tools_descartes_teastore_webui_servlet_AboutUsServlet",
                "SERVICE-HOST/about[GET]", "SERVICE-HOST/about[GET]");
        this.assertComponentProvidesOperation("tools_descartes_teastore_webui_servlet_CartActionServlet",
                "SERVICE-HOST/cartAction[GET]", "SERVICE-HOST/cartAction[GET]");
    }
}
