package org.palladiosimulator.somox.analyzer.rules.workflow;

import org.eclipse.core.runtime.IProgressMonitor;
import org.palladiosimulator.pcm.confidentiality.reverseengineering.feature.staticcodeanalysis.parts.SnykCLIStaticCodeAnalyst;
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineConfiguration;

import de.uka.ipd.sdq.workflow.extension.AbstractExtendableJob;

public class SnykCodeAnalystJob extends AbstractExtendableJob<RuleEngineBlackboard> {
    private final RuleEngineConfiguration configuration;

	public SnykCodeAnalystJob(RuleEngineConfiguration configuration, RuleEngineBlackboard blackboard) {
		this.configuration = configuration;
		setName("Snyk Code Analyst Job");
		setBlackboard(blackboard);
	}
	
	@Override
	public void execute(IProgressMonitor monitor) {
		SnykCLIStaticCodeAnalyst analyst = new SnykCLIStaticCodeAnalyst(configuration.getSnykPath());
		analyst.analyze(getBlackboard().getEntityPaths());
	}
}
