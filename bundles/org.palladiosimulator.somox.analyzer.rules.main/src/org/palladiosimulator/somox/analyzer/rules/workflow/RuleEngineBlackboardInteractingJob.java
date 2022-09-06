package org.palladiosimulator.somox.analyzer.rules.workflow;

import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineConfiguration;
import org.palladiosimulator.somox.analyzer.rules.main.RuleEngineAnalyzer;
import org.palladiosimulator.somox.analyzer.rules.main.RuleEngineException;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class RuleEngineBlackboardInteractingJob extends AbstractBlackboardInteractingJob<RuleEngineBlackboard> {

    private static final String NAME = "Rule Engine Blackboard Interacting";

    private final RuleEngineConfiguration configuration;

    public RuleEngineBlackboardInteractingJob(RuleEngineConfiguration configuration, RuleEngineBlackboard blackboard) {
        this.configuration = Objects.requireNonNull(configuration);
        setBlackboard(blackboard);
    }

    @Override
    public void cleanup(IProgressMonitor monitor) throws CleanupFailedException {
    }

    @Override
    public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
        try {
            new RuleEngineAnalyzer(getBlackboard()).analyze(configuration, monitor);
        } catch (final RuleEngineException e) {
            throw new JobFailedException(NAME + " Failed", e);
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
