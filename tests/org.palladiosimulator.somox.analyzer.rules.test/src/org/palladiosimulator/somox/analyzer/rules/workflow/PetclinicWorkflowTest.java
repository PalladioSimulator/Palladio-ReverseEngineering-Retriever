package org.palladiosimulator.somox.analyzer.rules.workflow;

import java.io.IOException;

import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;

import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class PetclinicWorkflowTest extends RuleEngineWorkflowTest {
    public PetclinicWorkflowTest() throws JobFailedException, UserCanceledException, IOException {
        super("external/spring-petclinic-microservices-2.3.6", DefaultRule.SPRING);
    }
}
