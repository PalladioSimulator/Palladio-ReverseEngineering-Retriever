package org.palladiosimulator.somox.analyzer.rules.mocore.workflow;

import java.io.File;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
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

    private Blackboard<Object> blackboard;
    private final String repositoryKey;
    private final String systemKey;
    private final String allocationKey;
    private final String resourceEnvironmentKey;
    private final File outputFilePrefix;

    public PersistenceJob(Blackboard<Object> blackboard, URI inputFolder, URI outputFolder, String repositoryKey,
            String systemKey, String allocationKey, String resourceEnvironmentKey) {
        this.blackboard = Objects.requireNonNull(blackboard);
        String configuredInputProjectName = inputFolder.lastSegment();
        this.repositoryKey = Objects.requireNonNull(repositoryKey);
        this.systemKey = Objects.requireNonNull(systemKey);
        this.allocationKey = Objects.requireNonNull(allocationKey);
        this.resourceEnvironmentKey = Objects.requireNonNull(resourceEnvironmentKey);
        // Set path to output folder and prefix of all output files to name of input project folder
        this.outputFilePrefix = new File(outputFolder.toFileString(), configuredInputProjectName);
    }

    @Override
    public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
        // Fetch input from blackboard
        monitor.subTask("Retrieving job input from blackboard");
        Repository repository = (Repository) this.blackboard.getPartition(repositoryKey);
        System system = (System) this.blackboard.getPartition(systemKey);
        Allocation allocation = (Allocation) this.blackboard.getPartition(allocationKey);
        ResourceEnvironment resourceEnvironment = (ResourceEnvironment) this.blackboard
                .getPartition(resourceEnvironmentKey);

        // Make blackboard models persistent by saving them as files
        monitor.subTask("Persisting models");
        ModelSaver.saveRepository(repository, outputFilePrefix.getPath(), false);
        ModelSaver.saveSystem(system, outputFilePrefix.getPath(), false);
        ModelSaver.saveAllocation(allocation, outputFilePrefix.getPath(), false);
        ModelSaver.saveResourceEnvironment(resourceEnvironment, outputFilePrefix.getPath(), false);
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