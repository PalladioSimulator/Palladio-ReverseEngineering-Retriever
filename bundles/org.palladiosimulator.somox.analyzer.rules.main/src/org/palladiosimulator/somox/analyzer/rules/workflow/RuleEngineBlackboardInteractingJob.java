package org.palladiosimulator.somox.analyzer.rules.workflow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineAnalyzerConfiguration;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineConfiguration;
import org.palladiosimulator.somox.analyzer.rules.main.RuleEngineAnalyzer;
import org.somox.analyzer.ModelAnalyzerException;
import org.somox.extractor.ExtractionResult;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class RuleEngineBlackboardInteractingJob extends AbstractBlackboardInteractingJob<RuleEngineBlackboard> {
	
    private static final String NAME = "Rule Engine Blackboard Interacting";

    private final RuleEngineConfiguration configuration;
    private final HashMap<String, ExtractionResult> extractionResults;

    public RuleEngineBlackboardInteractingJob(RuleEngineAnalyzerConfiguration configuration, RuleEngineBlackboard blackboard) {
        this(configuration.getMoxConfiguration(), blackboard);
    }

    public RuleEngineBlackboardInteractingJob(RuleEngineConfiguration configuration, RuleEngineBlackboard blackboard) {
        this.configuration = Objects.requireNonNull(configuration);
        setBlackboard(blackboard);
        extractionResults = new HashMap<>();
    }

    @Override
    public void cleanup(IProgressMonitor monitor) throws CleanupFailedException {
        // TODO check whether this has no side effects and/or is necessary to clean up
        // extractionResults.clear();
    }

    @Override
    public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
        try {
            myBlackboard.setAnalysisResult(new RuleEngineAnalyzer(getBlackboard()).analyze(configuration, extractionResults, monitor));
        } catch (final ModelAnalyzerException e) {
            throw new JobFailedException(NAME + " Failed", e);
        }
    }

    public Map<String, ExtractionResult> getExtractionResults() {
        return Collections.unmodifiableMap(extractionResults);
    }

    @Override
    public String getName() {
        return NAME;
    }

}
