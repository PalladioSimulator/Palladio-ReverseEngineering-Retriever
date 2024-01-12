package org.palladiosimulator.retriever.mocore.workflow;

import java.util.Collection;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.retriever.mocore.discovery.RepositoryDecompositor;
import org.palladiosimulator.retriever.mocore.orchestration.PcmOrchestrator;
import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.transformation.AllocationTransformer;
import org.palladiosimulator.retriever.mocore.transformation.RepositoryTransformer;
import org.palladiosimulator.retriever.mocore.transformation.ResourceEnvironmentTransformer;
import org.palladiosimulator.retriever.mocore.transformation.SystemTransformer;

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

    public MoCoReJob(final Blackboard<Object> blackboard, final String repositoryInputKey,
            final String repositoryOutputKey, final String systemOutputKey, final String allocationOutputKey,
            final String resourceEnvironmentOutputKey) {
        this.blackboard = Objects.requireNonNull(blackboard);
        this.repositoryInputKey = Objects.requireNonNull(repositoryInputKey);
        this.repositoryOutputKey = Objects.requireNonNull(repositoryOutputKey);
        this.systemOutputKey = Objects.requireNonNull(systemOutputKey);
        this.allocationOutputKey = Objects.requireNonNull(allocationOutputKey);
        this.resourceEnvironmentOutputKey = Objects.requireNonNull(resourceEnvironmentOutputKey);
    }

    @Override
    public void execute(final IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
        // Fetch input from blackboard
        monitor.subTask("Retrieving job input from blackboard");
        final Repository inputRepository = (Repository) this.blackboard.getPartition(this.repositoryInputKey);

        // Convert input into processable discoverers
        monitor.subTask("Converting input into processable discoveries");
        final RepositoryDecompositor repositoryDecompositor = new RepositoryDecompositor();
        final Collection<Discoverer<?>> discoverers = repositoryDecompositor.decompose(inputRepository);

        // Composite & refine discoveries via PCM orchestrator
        monitor.subTask("Processing discoveries");
        final PcmOrchestrator orchestrator = new PcmOrchestrator();
        discoverers.forEach(orchestrator::processDiscoverer);

        // Transform surrogate model into PCM models
        monitor.subTask("Transforming surrogate model into output models");
        final PcmSurrogate surrogate = orchestrator.getModel();
        final Repository repository = new RepositoryTransformer().transform(surrogate);
        final System system = new SystemTransformer().transform(surrogate, repository);
        final ResourceEnvironment resourceEnvironment = new ResourceEnvironmentTransformer().transform(surrogate);
        final Allocation allocation = new AllocationTransformer().transform(surrogate, system, resourceEnvironment);

        // Add transformed models to blackboard
        monitor.subTask("Adding output models to blackboard");
        this.blackboard.addPartition(this.repositoryOutputKey, repository);
        this.blackboard.addPartition(this.systemOutputKey, system);
        this.blackboard.addPartition(this.allocationOutputKey, allocation);
        this.blackboard.addPartition(this.resourceEnvironmentOutputKey, resourceEnvironment);
        monitor.done();
    }

    @Override
    public void cleanup(final IProgressMonitor monitor) throws CleanupFailedException {
        // No cleanup required for the job
    }

    @Override
    public String getName() {
        return JOB_NAME;
    }

    @Override
    public void setBlackboard(final Blackboard<Object> blackboard) {
        this.blackboard = Objects.requireNonNull(blackboard);
    }
}
