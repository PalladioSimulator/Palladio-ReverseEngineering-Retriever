package org.palladiosimulator.somox.analyzer.rules.engine.test;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;

public class TeaStoreTest extends RuleEngineTest {

    protected TeaStoreTest() {
        // TODO remove the second argument when the test is re-enabled!
        super("TeaStore", false, DefaultRule.JAX_RS);
    }

    /**
     * Tests the basic functionality of the RuleEngineAnalyzer when executing the JAX_RS rule.
     * Requires it to execute without an exception and produce an output file with the correct contents.
     */
    @Test
    @Disabled("TeaStore will be dealt with later, they had a big update")
    void test() {
        /*
        assertEquals(1, getComponents().size());
        assertEquals(1, getDatatypes().size());
        assertEquals(0, getFailuretypes().size());
        assertEquals(0, getInterfaces().size());
        */
    }
}
