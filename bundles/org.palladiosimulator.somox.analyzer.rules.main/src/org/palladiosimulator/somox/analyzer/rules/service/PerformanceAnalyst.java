package org.palladiosimulator.somox.analyzer.rules.service;

import java.util.Collections;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineConfiguration;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

/**
 * An analyst that currently does nothing. It is reserved for later use.
 * 
 * @author Florian Bossert
 */
public class PerformanceAnalyst implements Analyst {
    private static final String ANALYST_ID = "org.palladiosimulator.somox.analyzer.rules.service.performance_analyst";

    @Override
    public IBlackboardInteractingJob<RuleEngineBlackboard> create(RuleEngineConfiguration configuration,
            RuleEngineBlackboard blackboard) {
        return new AbstractBlackboardInteractingJob<>() {
            @Override
            public void cleanup(final IProgressMonitor monitor) throws CleanupFailedException {
            }

            @Override
            public void execute(final IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
                // Do nothing
            }

            @Override
            public String getName() {
                return "Performance Analyst Job";
            }
        };
    }

    @Override
    public Set<String> getConfigurationKeys() {
        return Collections.emptySet();
    }

    @Override
    public String getName() {
        return "Performance Analyst";
    }

    @Override
    public String getID() {
        return ANALYST_ID;
    }

}
