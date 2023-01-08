package org.palladiosimulator.somox.analyzer.rules.mocore.workflow;

import java.util.Objects;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.somox.analyzer.rules.mocore.orchestration.PcmOrchestrator;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.transformation.AllocationTransformer;
import org.palladiosimulator.somox.analyzer.rules.mocore.transformation.RepositoryTransformer;
import org.palladiosimulator.somox.analyzer.rules.mocore.transformation.ResourceEnvironmentTransformer;
import org.palladiosimulator.somox.analyzer.rules.mocore.transformation.SystemTransformer;

import de.uka.ipd.sdq.workflow.blackboard.Blackboard;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;
import tools.mdsd.mocore.framework.discovery.Discoverer;

public class MoCoReJob implements IBlackboardInteractingJob<Blackboard<Object>> {
    private static final String JOB_NAME = "Model Composition & Refinement Job";
    private static final String BLACKBOARD_INPUT_ID_REPOSITORY = "repository";
    private static final String BLACKBOARD_OUTPUT_ID_REPOSITORY = "repository";
    private static final String BLACKBOARD_OUTPUT_ID_SYSTEM = "system";
    private static final String BLACKBOARD_OUTPUT_ID_ALLOCATION = "allocation";
    private static final String BLACKBOARD_OUTPUT_ID_RESOURCE_ENVIRONMENT = "resource";

    private Blackboard<Object> blackboard;

    public MoCoReJob(Blackboard<Object> blackboard) {
        this.blackboard = Objects.requireNonNull(blackboard);
    }

    @Override
    public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
        // Fetch input from blackboard
        monitor.subTask("Retrieving job input from blackboard");
        Repository inputRepository = (Repository) this.blackboard.getPartition(BLACKBOARD_INPUT_ID_REPOSITORY);

        // TODO Convert input into processable discoverers
        monitor.subTask("Converting input into processable discoveries");
        Set<Discoverer<?>> discoverers = null;

        // Composite & refine discoveries via PCM orchestrator
        monitor.subTask("Processing discoveries");
        PcmOrchestrator orchestrator = new PcmOrchestrator();
        discoverers.forEach(orchestrator::processDiscoverer);

        // Transform surrogate model into PCM models
        monitor.subTask("Transforming surrogate model into output models");
        PcmSurrogate surrogate = orchestrator.getModel();
        Repository repository = new RepositoryTransformer().transform(surrogate);
        System system = new SystemTransformer().transform(surrogate);
        Allocation allocation = new AllocationTransformer().transform(surrogate);
        ResourceEnvironment resourceEnvironment = new ResourceEnvironmentTransformer().transform(surrogate);

        // Add transformed models to blackboard
        monitor.subTask("Adding output models to blackboard");
        this.blackboard.addPartition(BLACKBOARD_OUTPUT_ID_REPOSITORY, repository);
        this.blackboard.addPartition(BLACKBOARD_OUTPUT_ID_SYSTEM, system);
        this.blackboard.addPartition(BLACKBOARD_OUTPUT_ID_ALLOCATION, allocation);
        this.blackboard.addPartition(BLACKBOARD_OUTPUT_ID_RESOURCE_ENVIRONMENT, resourceEnvironment);
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
