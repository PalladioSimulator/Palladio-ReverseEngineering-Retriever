package org.palladiosimulator.somox.analyzer.rules.mocore.workflow;

import java.io.File;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.palladiosimulator.generator.fluent.shared.util.ModelSaver;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.system.System;

import de.uka.ipd.sdq.workflow.blackboard.Blackboard;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class PersistenceJob implements IBlackboardInteractingJob<Blackboard<Object>> {
    private static final String JOB_NAME = "Model Persistence Job";
    private static final String BLACKBOARD_INPUT_ID_REPOSITORY = "repository";
    private static final String BLACKBOARD_INPUT_ID_SYSTEM = "system";
    private static final String BLACKBOARD_INPUT_ID_ALLOCATION = "allocation";
    private static final String BLACKBOARD_INPUT_ID_RESOURCE_ENVIRONMENT = "resource";

    private Blackboard<Object> blackboard;
    private String path;

    public PersistenceJob(Blackboard<Object> blackboard, String path) {
        this.blackboard = Objects.requireNonNull(blackboard);
        this.path = new File(Objects.requireNonNull(path), "mocore").toString();
    }

    @Override
    public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
        // Fetch input from blackboard
        monitor.subTask("Retrieving job input from blackboard");
        Repository repository = (Repository) this.blackboard.getPartition(BLACKBOARD_INPUT_ID_REPOSITORY);
        System system = (System) this.blackboard.getPartition(BLACKBOARD_INPUT_ID_SYSTEM);
        Allocation allocation = (Allocation) this.blackboard.getPartition(BLACKBOARD_INPUT_ID_ALLOCATION);
        ResourceEnvironment resourceEnvironment = (ResourceEnvironment) this.blackboard
                .getPartition(BLACKBOARD_INPUT_ID_RESOURCE_ENVIRONMENT);

        // Make blackboard models persistent by saving them as files
        monitor.subTask("Persisting models");
        ModelSaver.saveRepository(repository, this.path, false);
        ModelSaver.saveSystem(system, this.path, false);
        ModelSaver.saveAllocation(allocation, this.path, false);
        ModelSaver.saveResourceEnvironment(resourceEnvironment, this.path, false);
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
