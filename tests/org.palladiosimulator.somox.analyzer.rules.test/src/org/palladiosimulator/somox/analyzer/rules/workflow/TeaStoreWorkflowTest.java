package org.palladiosimulator.somox.analyzer.rules.workflow;

import java.io.IOException;

import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;

import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class TeaStoreWorkflowTest extends RuleEngineWorkflowTest {
    public TeaStoreWorkflowTest() throws JobFailedException, UserCanceledException, IOException {
        super("external/TeaStore-1.4.1", DefaultRule.JAX_RS);
    }
}
