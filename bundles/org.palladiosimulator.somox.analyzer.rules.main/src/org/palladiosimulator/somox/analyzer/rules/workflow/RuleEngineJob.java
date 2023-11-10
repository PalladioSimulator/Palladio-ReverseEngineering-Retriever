package org.palladiosimulator.somox.analyzer.rules.workflow;

import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineBlackboardKeys;
import org.palladiosimulator.somox.analyzer.rules.engine.Rule;
import org.palladiosimulator.somox.analyzer.rules.engine.RuleEngineConfiguration;
import org.palladiosimulator.somox.analyzer.rules.mocore.workflow.MoCoReJob;
import org.palladiosimulator.somox.analyzer.rules.service.Analyst;
import org.palladiosimulator.somox.ast2seff.jobs.Ast2SeffJob;
import org.palladiosimulator.somox.discoverer.Discoverer;

import de.uka.ipd.sdq.workflow.extension.AbstractExtendableJob;
import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.ParallelJob;

public class RuleEngineJob extends AbstractExtendableJob<RuleEngineBlackboard> {

    public RuleEngineJob(RuleEngineConfiguration configuration) {
        super.setBlackboard(new RuleEngineBlackboard());

        super.add(createDiscoverersJob(configuration));

        super.add(createRulesJob(configuration));

        super.add(createBuildRulesJob(configuration));

        super.add(new RuleEngineBlackboardInteractingJob(configuration, getBlackboard()));

        super.add(createAnalystsJob(configuration));

        // Generate service effect specifications based on AST nodes and merge them into repository
        super.add(
                new Ast2SeffJob(getBlackboard(), RuleEngineBlackboardKeys.RULE_ENGINE_BLACKBOARD_KEY_SEFF_ASSOCIATIONS,
                        RuleEngineBlackboardKeys.RULE_ENGINE_AST2SEFF_OUTPUT_REPOSITORY));
        super.add(new SeffMergerJob(myBlackboard, RuleEngineBlackboardKeys.RULE_ENGINE_AST2SEFF_OUTPUT_REPOSITORY,
                RuleEngineBlackboardKeys.RULE_ENGINE_BLACKBOARD_KEY_REPOSITORY));

        // Refine model and create final repository, system, allocation, & resource environment
        super.add(new MoCoReJob(getBlackboard(), RuleEngineBlackboardKeys.RULE_ENGINE_BLACKBOARD_KEY_REPOSITORY,
                RuleEngineBlackboardKeys.RULE_ENGINE_MOCORE_OUTPUT_REPOSITORY,
                RuleEngineBlackboardKeys.RULE_ENGINE_MOCORE_OUTPUT_SYSTEM,
                RuleEngineBlackboardKeys.RULE_ENGINE_MOCORE_OUTPUT_ALLOCATION,
                RuleEngineBlackboardKeys.RULE_ENGINE_MOCORE_OUTPUT_RESOURCE_ENVIRONMENT));

        // Merge data & failure types into output repository
        super.add(new TypeMergerJob(getBlackboard(), RuleEngineBlackboardKeys.RULE_ENGINE_BLACKBOARD_KEY_REPOSITORY,
                RuleEngineBlackboardKeys.RULE_ENGINE_MOCORE_OUTPUT_REPOSITORY));

        // Persist repository, system, allocation, & resource environment model from blackboard into
        // file system
        super.add(new PersistenceJob(getBlackboard(), configuration.getInputFolder(), configuration.getOutputFolder(),
                RuleEngineBlackboardKeys.RULE_ENGINE_MOCORE_OUTPUT_REPOSITORY,
                RuleEngineBlackboardKeys.RULE_ENGINE_MOCORE_OUTPUT_SYSTEM,
                RuleEngineBlackboardKeys.RULE_ENGINE_MOCORE_OUTPUT_ALLOCATION,
                RuleEngineBlackboardKeys.RULE_ENGINE_MOCORE_OUTPUT_RESOURCE_ENVIRONMENT));

        super.add(new PlantUmlJob(configuration, getBlackboard()));
    }
    
    private ParallelJob createRulesJob(RuleEngineConfiguration configuration) {
        ParallelJob parentJob = new ParallelJob();
        
        for (Rule rule : configuration.getConfig(Rule.class)
            .getSelected()) {
        	if (!rule.isBuildRule()) {
        		IBlackboardInteractingJob<RuleEngineBlackboard> ruleJob = rule.create(configuration, myBlackboard);
        		parentJob.add(ruleJob);
            	logger.info("Adding rule job \"" + ruleJob.getName() + "\"");
        	}
        }
        
        return parentJob;
    }

    private ParallelJob createBuildRulesJob(RuleEngineConfiguration configuration) {
        ParallelJob parentJob = new ParallelJob();
        
        for (Rule rule : configuration.getConfig(Rule.class)
            .getSelected()) {
        	if (rule.isBuildRule()) {
        		IBlackboardInteractingJob<RuleEngineBlackboard> ruleJob = rule.create(configuration, myBlackboard);
        		parentJob.add(ruleJob);
        		logger.info("Adding build rule job \"" + ruleJob.getName() + "\"");
        	}
        }

        return parentJob;
    }

    private ParallelJob createDiscoverersJob(RuleEngineConfiguration configuration) {
        ParallelJob parentJob = new ParallelJob();
        for (Discoverer discoverer : configuration.getConfig(Discoverer.class)
            .getSelected()) {
            IBlackboardInteractingJob<RuleEngineBlackboard> discovererJob = discoverer.create(configuration,
                    myBlackboard);
            parentJob.add(discovererJob);
            logger.info("Adding discoverer job \"" + discovererJob.getName() + "\"");
        }
        return parentJob;
    }

    private ParallelJob createAnalystsJob(RuleEngineConfiguration configuration) {
        ParallelJob parentJob = new ParallelJob();
        for (Analyst analyst : configuration.getConfig(Analyst.class)
            .getSelected()) {
            IBlackboardInteractingJob<RuleEngineBlackboard> analystJob = analyst.create(configuration, myBlackboard);
            parentJob.add(analystJob);
            logger.info("Adding analyst job \"" + analystJob.getName() + "\"");
        }
        return parentJob;
    }
}
