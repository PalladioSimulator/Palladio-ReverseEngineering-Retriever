package org.palladiosimulator.somox.analyzer.rules.mocore.workflow;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import de.uka.ipd.sdq.workflow.blackboard.Blackboard;

public class MoCoReJobTest {
    private final static String BLACKBOARD_OUTPUT_REPOSITORY = "repository";
    private final static String BLACKBOARD_INPUT_REPOSITORY = "repository";
    private final static String BLACKBOARD_INPUT_SYSTEM = "system";
    private final static String BLACKBOARD_INPUT_ALLOCATION = "allocation";
    private final static String BLACKBOARD_INPUT_RESOURCEENVIRONMENT = "resource";

    @Test
    public void testConstructorWithValidInput() {
        Blackboard<Object> blackboard = new Blackboard<Object>();
        assertDoesNotThrow(() -> new MoCoReJob(blackboard, BLACKBOARD_INPUT_REPOSITORY,
                BLACKBOARD_OUTPUT_REPOSITORY, BLACKBOARD_INPUT_SYSTEM, BLACKBOARD_INPUT_ALLOCATION,
                BLACKBOARD_INPUT_RESOURCEENVIRONMENT));
    }
}
