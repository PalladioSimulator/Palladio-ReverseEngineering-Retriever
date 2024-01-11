package org.palladiosimulator.retriever.core.workflow;

import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.palladiosimulator.pcm.repository.Repository;

import de.uka.ipd.sdq.workflow.blackboard.Blackboard;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class TypeMergerJob implements IBlackboardInteractingJob<Blackboard<Object>> {
    private static final String JOB_NAME = "DataType & FailureType Repository Merger Job";

    private Blackboard<Object> blackboard;

    private final String sourceTypeRepositoryKey;
    private final String destinationTypeRepositoryKey;

    public TypeMergerJob(Blackboard<Object> blackboard, String sourceTypeRepositoryKey,
            String destinationTypeRepositoryKey) {
        this.blackboard = Objects.requireNonNull(blackboard);
        this.sourceTypeRepositoryKey = sourceTypeRepositoryKey;
        this.destinationTypeRepositoryKey = destinationTypeRepositoryKey;
    }

    @Override
    public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
        // Fetch input from blackboard
        monitor.subTask("Retrieving source and destination repository from blackboard");
        Repository sourceRepository = (Repository) this.blackboard.getPartition(this.sourceTypeRepositoryKey);
        Repository destinationRepository = (Repository) this.blackboard.getPartition(this.destinationTypeRepositoryKey);

        // Move types from source to destination repository
        monitor.subTask("Merging types from source into destination repository");
        destinationRepository.getDataTypes__Repository()
            .addAll(sourceRepository.getDataTypes__Repository());
        destinationRepository.getFailureTypes__Repository()
            .addAll(sourceRepository.getFailureTypes__Repository());
        monitor.done();
    }

    @Override
    public void cleanup(IProgressMonitor monitor) throws CleanupFailedException {
        // No cleanup required for the job
    }

    @Override
    public String getName() {
        return JOB_NAME;
    }

    @Override
    public void setBlackboard(Blackboard<Object> blackboard) {
        this.blackboard = Objects.requireNonNull(blackboard);
    }
}
