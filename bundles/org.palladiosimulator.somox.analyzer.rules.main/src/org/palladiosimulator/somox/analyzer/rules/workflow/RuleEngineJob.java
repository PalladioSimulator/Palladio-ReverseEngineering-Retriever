package org.palladiosimulator.somox.analyzer.rules.workflow;

import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineAnalyzerConfiguration;

import de.uka.ipd.sdq.workflow.extension.AbstractExtendableJob;

public class RuleEngineJob extends AbstractExtendableJob<RuleEngineBlackboard> {

    public RuleEngineJob(RuleEngineAnalyzerConfiguration configuration) {
        setBlackboard(new RuleEngineBlackboard());

        this.add(new RuleEngineBlackboardInteractingJob(configuration));

        // TODO
        // this.add(new SeffCreatorJob(false, null, null));

        this.add(new ModelSaverJob(configuration.getMoxConfiguration()));

    }

}
