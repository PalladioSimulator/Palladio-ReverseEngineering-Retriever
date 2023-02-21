package org.palladiosimulator.somox.analyzer.rules.trace;

import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;

import de.uka.ipd.sdq.workflow.blackboard.Blackboard;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class TraceJob implements IBlackboardInteractingJob<Blackboard<Object>> {

	private Blackboard<Object> blackboard;
	private ArtifactTraceCreator creator;

	public TraceJob(Blackboard<Object> blackboard) {
		setBlackboard(blackboard);
		creator = new ArtifactTraceCreator();
	}

	@Override
	public void cleanup(IProgressMonitor monitor) throws CleanupFailedException {
		blackboard = null;
		creator = null;
	}

	@Override
	public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
		// TODO Auto-generated method stub
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public void setBlackboard(Blackboard<Object> blackboard) {
		this.blackboard = Objects.requireNonNull(blackboard);
	}

}
