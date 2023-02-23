package org.palladiosimulator.somox.analyzer.rules.trace;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.palladiosimulator.pcm.core.entity.Entity;
import org.palladiosimulator.somox.analyzer.rules.blackboard.CompilationUnitWrapper;
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineConfiguration;

import de.uka.ipd.sdq.workflow.blackboard.Blackboard;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class ArtifactTraceJob implements IBlackboardInteractingJob<Blackboard<Object>> {

	public static final String ARTIFACT_TRACE_CREATOR = ArtifactTraceCreator.class.getCanonicalName();
	private RuleEngineBlackboard blackboard;
	private RuleEngineConfiguration configuration;
	private final ArtifactTraceCreator creator;

	public ArtifactTraceJob(RuleEngineConfiguration configuration, RuleEngineBlackboard blackboard) {
		setBlackboard(blackboard);
		setConfiguration(configuration);

		// Paths.get(this.configuration.getInputFolder().devicePath()).normalize().getFileName().toString()
		creator = new ArtifactTraceCreator(this.configuration.getInputFolder());
		this.blackboard.addPartition(ARTIFACT_TRACE_CREATOR, creator);
	}

	@Override
	public void cleanup(IProgressMonitor monitor) throws CleanupFailedException {
	}

	@Override
	public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {

		// Components to source code
		final Map<Entity, Path> entities = blackboard.getEntityPaths();
		for (final Entity key : entities.keySet()) {
			creator.addTrace(key, entities.get(key));
		}

		// Build to code
		final Map<Path, Set<CompilationUnitWrapper>> associations = blackboard.getSystemAssociations();
		for (final Path path : associations.keySet()) {
			for (final CompilationUnitWrapper unit : associations.get(path)) {
				creator.addTrace(path, blackboard.getCompilationUnitLocation(unit));
			}
		}

		// TODO
		// blackboard.getEclipsePCMDetector().

	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public void setBlackboard(Blackboard<Object> blackboard) {
		if ((blackboard == null) || !(blackboard instanceof final RuleEngineBlackboard board)) {
			throw new IllegalArgumentException();
		}
		this.blackboard = board;
	}

	public void setConfiguration(RuleEngineConfiguration configuration) {
		this.configuration = Objects.requireNonNull(configuration);
	}

}
