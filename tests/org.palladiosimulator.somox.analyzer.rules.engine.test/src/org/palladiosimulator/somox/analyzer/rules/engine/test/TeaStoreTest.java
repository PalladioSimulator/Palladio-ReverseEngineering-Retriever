package org.palladiosimulator.somox.analyzer.rules.engine.test;

import org.junit.jupiter.api.Disabled;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;

public class TeaStoreTest extends RuleEngineTest {

    protected TeaStoreTest() {
        super("external/TeaStore-master", DefaultRule.JAX_RS);
    }

    /**
     * Tests the basic functionality of the RuleEngineAnalyzer when executing the JAX_RS rule.
     * Requires it to execute without an exception and produce an output file with the correct contents.
     */
    @Disabled("TeaStore will be dealt with later, they had a big update")
    void test() {
    }
}
