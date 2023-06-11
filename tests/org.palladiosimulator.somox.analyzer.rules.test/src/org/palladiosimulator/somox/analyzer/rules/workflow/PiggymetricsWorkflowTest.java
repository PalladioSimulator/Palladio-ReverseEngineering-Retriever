package org.palladiosimulator.somox.analyzer.rules.workflow;

import java.io.IOException;

import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;

import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class PiggymetricsWorkflowTest extends RuleEngineJobTest {
    public PiggymetricsWorkflowTest() throws JobFailedException, UserCanceledException, IOException {
        super("external/piggymetrics-spring.version.2.0.3", DefaultRule.SPRING);
    }
}
