package org.palladiosimulator.retriever.core.workflow;

import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.palladiosimulator.retriever.core.main.Retriever;
import org.palladiosimulator.retriever.core.main.RetrieverException;
import org.palladiosimulator.retriever.services.RetrieverConfiguration;
import org.palladiosimulator.retriever.services.blackboard.RetrieverBlackboard;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class RetrieverBlackboardInteractingJob extends AbstractBlackboardInteractingJob<RetrieverBlackboard> {

    private static final String NAME = "Retriever Blackboard Interacting Job";

    private final RetrieverConfiguration configuration;

    public RetrieverBlackboardInteractingJob(final RetrieverConfiguration configuration,
            final RetrieverBlackboard blackboard) {
        super.setBlackboard(blackboard);
        this.configuration = Objects.requireNonNull(configuration);
    }

    @Override
    public void cleanup(final IProgressMonitor monitor) throws CleanupFailedException {
    }

    @Override
    public void execute(final IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
        try {
            new Retriever(this.getBlackboard()).analyze(this.configuration, monitor);
        } catch (final RetrieverException e) {
            throw new JobFailedException(NAME + " Failed", e);
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
