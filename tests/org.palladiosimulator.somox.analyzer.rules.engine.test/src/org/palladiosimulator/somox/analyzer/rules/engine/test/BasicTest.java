package org.palladiosimulator.somox.analyzer.rules.engine.test;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;

public class BasicTest extends RuleEngineTest {

    protected BasicTest() {
        super("BasicProject", new HashSet<DefaultRule>());
    }

    /**
     * Tests the basic functionality of the RuleEngineAnalyzer.
     * Requires it to execute without an exception and produce an output file.
     */
    void test() {
        assertTrue(OUT_DIR.resolve("pcm.repository").toFile().exists());
    }
}
