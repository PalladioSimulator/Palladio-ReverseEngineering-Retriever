package org.palladiosimulator.somox.analyzer.rules.workflow;

import java.io.IOException;

import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;

import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class ACMEWorkflowTest extends RuleEngineJobTest {
    public ACMEWorkflowTest() throws JobFailedException, UserCanceledException, IOException {
        super("external/acmeair-1.2.0", DefaultRule.JAX_RS);
    }
}
