package org.palladiosimulator.somox.analyzer.rules.mocore.workflow;

import java.util.Collection;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.somox.analyzer.rules.mocore.discovery.RepositoryDecompositor;
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

    private Blackboard<Object> blackboard;
    private final String repositoryInputKey;
    private final String repositoryOutputKey;
    private final String systemOutputKey;
    private final String allocationOutputKey;
    private final String resourceEnvironmentOutputKey;

    public MoCoReJob(Blackboard<Object> blackboard, String repositoryInputKey, String repositoryOutputKey,
            String systemOutputKey, String allocationOutputKey, String resourceEnvironmentOutputKey) {
        this.blackboard = Objects.requireNonNull(blackboard);
        this.repositoryInputKey = Objects.requireNonNull(repositoryInputKey);
        this.repositoryOutputKey = Objects.requireNonNull(repositoryOutputKey);
        this.systemOutputKey = Objects.requireNonNull(systemOutputKey);
        this.allocationOutputKey = Objects.requireNonNull(allocationOutputKey);
        this.resourceEnvironmentOutputKey = Objects.requireNonNull(resourceEnvironmentOutputKey);
    }

    @Override
    public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
        // Fetch input from blackboard
        monitor.subTask("Retrieving job input from blackboard");
        Repository inputRepository = (Repository) this.blackboard.getPartition(repositoryInputKey);

        // Convert input into processable discoverers
        monitor.subTask("Converting input into processable discoveries");
        RepositoryDecompositor repositoryDecompositor = new RepositoryDecompositor();
        Collection<Discoverer<?>> discoverers = repositoryDecompositor.decompose(inputRepository);

        // Composite & refine discoveries via PCM orchestrator
        monitor.subTask("Processing discoveries");
        PcmOrchestrator orchestrator = new PcmOrchestrator();
        discoverers.forEach(orchestrator::processDiscoverer);

        // Transform surrogate model into PCM models
        monitor.subTask("Transforming surrogate model into output models");
        PcmSurrogate surrogate = orchestrator.getModel();
        Repository repository = new RepositoryTransformer().transform(surrogate);
        System system = new SystemTransformer().transform(surrogate, repository);
        ResourceEnvironment resourceEnvironment = new ResourceEnvironmentTransformer().transform(surrogate);
        Allocation allocation = new AllocationTransformer().transform(surrogate, system, resourceEnvironment);

        // Add transformed models to blackboard
        monitor.subTask("Adding output models to blackboard");
        this.blackboard.addPartition(repositoryOutputKey, repository);
        this.blackboard.addPartition(systemOutputKey, system);
        this.blackboard.addPartition(allocationOutputKey, allocation);
        this.blackboard.addPartition(resourceEnvironmentOutputKey, resourceEnvironment);
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
