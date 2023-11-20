package org.palladiosimulator.somox.analyzer.rules.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

        super.addAll(createDiscovererJobs(configuration));

        super.addAll(createRuleJobs(configuration));

        super.addAll(createBuildRulesJob(configuration));

        super.add(new RuleEngineBlackboardInteractingJob(configuration, getBlackboard()));

        super.addAll(createAnalystJobs(configuration));

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
    
    private List<ParallelJob> createRuleJobs(RuleEngineConfiguration configuration) {
    	List<ParallelJob> jobs = new ArrayList<>();

        for (Collection<Rule> step : configuration.getConfig(Rule.class).getExecutionOrder()) {
        	ParallelJob parentJob = new ParallelJob();
        	for (Rule rule : step) {
        		// Assume only build rules depend on build rules.
        		if (rule.isBuildRule()) {
        			continue;
        		}
        		IBlackboardInteractingJob<RuleEngineBlackboard> ruleJob = rule.create(configuration, myBlackboard);
        		parentJob.add(ruleJob);
        		logger.info("Adding rule job \"" + ruleJob.getName() + "\"");
        	}
        	jobs.add(parentJob);
        }
        
        return jobs;
    }

    private List<ParallelJob> createBuildRulesJob(RuleEngineConfiguration configuration) {
    	List<ParallelJob> jobs = new ArrayList<>();

        for (Collection<Rule> step : configuration.getConfig(Rule.class).getExecutionOrder()) {
        	ParallelJob parentJob = new ParallelJob();
        	for (Rule rule : step) {
        		// Assume only build rules depend on build rules.
        		if (!rule.isBuildRule()) {
        			continue;
        		}
        		IBlackboardInteractingJob<RuleEngineBlackboard> ruleJob = rule.create(configuration, myBlackboard);
        		parentJob.add(ruleJob);
        		logger.info("Adding build rule job \"" + ruleJob.getName() + "\"");
        	}
        	jobs.add(parentJob);
        }
        
        return jobs;
    }

    private List<ParallelJob> createDiscovererJobs(RuleEngineConfiguration configuration) {
    	List<ParallelJob> jobs = new ArrayList<>();

        for (Collection<Discoverer> step : configuration.getConfig(Discoverer.class).getExecutionOrder()) {
        	ParallelJob parentJob = new ParallelJob();
        	for (Discoverer discoverer : step) {
        		IBlackboardInteractingJob<RuleEngineBlackboard> discovererJob = discoverer.create(configuration, myBlackboard);
        		parentJob.add(discovererJob);
        		logger.info("Adding discoverer job \"" + discovererJob.getName() + "\"");
        	}
        	jobs.add(parentJob);
        }
        
        return jobs;
    }

    private List<ParallelJob> createAnalystJobs(RuleEngineConfiguration configuration) {
    	List<ParallelJob> jobs = new ArrayList<>();

        for (Collection<Analyst> step : configuration.getConfig(Analyst.class).getExecutionOrder()) {
        	ParallelJob parentJob = new ParallelJob();
        	for (Analyst analyst : step) {
        		IBlackboardInteractingJob<RuleEngineBlackboard> analystJob = analyst.create(configuration, myBlackboard);
        		parentJob.add(analystJob);
        		logger.info("Adding analyst job \"" + analystJob.getName() + "\"");
        	}
        	jobs.add(parentJob);
        }
        
        return jobs;
    }
}
