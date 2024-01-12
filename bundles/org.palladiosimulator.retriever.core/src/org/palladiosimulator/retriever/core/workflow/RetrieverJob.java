package org.palladiosimulator.retriever.core.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.palladiosimulator.retriever.core.configuration.RetrieverBlackboardKeys;
import org.palladiosimulator.retriever.core.service.Analyst;
import org.palladiosimulator.retriever.extraction.blackboard.RetrieverBlackboard;
import org.palladiosimulator.retriever.extraction.engine.Discoverer;
import org.palladiosimulator.retriever.extraction.engine.Rule;
import org.palladiosimulator.retriever.extraction.engine.RetrieverConfiguration;
import org.palladiosimulator.retriever.mocore.workflow.MoCoReJob;
import org.palladiosimulator.somox.ast2seff.jobs.Ast2SeffJob;

import de.uka.ipd.sdq.workflow.extension.AbstractExtendableJob;
import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.ParallelJob;

public class RetrieverJob extends AbstractExtendableJob<RetrieverBlackboard> {

    public RetrieverJob(RetrieverConfiguration configuration) {
        super.setBlackboard(new RetrieverBlackboard());

        super.addAll(createDiscovererJobs(configuration));

        super.addAll(createRuleJobs(configuration));

        super.addAll(createBuildRulesJob(configuration));

        super.add(new RetrieverBlackboardInteractingJob(configuration, getBlackboard()));

        super.addAll(createAnalystJobs(configuration));

        // Generate service effect specifications based on AST nodes and merge them into repository
        super.add(
                new Ast2SeffJob(getBlackboard(), RetrieverBlackboardKeys.RULE_ENGINE_BLACKBOARD_KEY_SEFF_ASSOCIATIONS,
                        RetrieverBlackboardKeys.RULE_ENGINE_AST2SEFF_OUTPUT_REPOSITORY));
        super.add(new SeffMergerJob(myBlackboard, RetrieverBlackboardKeys.RULE_ENGINE_AST2SEFF_OUTPUT_REPOSITORY,
                RetrieverBlackboardKeys.RULE_ENGINE_BLACKBOARD_KEY_REPOSITORY));

        // Refine model and create final repository, system, allocation, & resource environment
        super.add(new MoCoReJob(getBlackboard(), RetrieverBlackboardKeys.RULE_ENGINE_BLACKBOARD_KEY_REPOSITORY,
                RetrieverBlackboardKeys.RULE_ENGINE_MOCORE_OUTPUT_REPOSITORY,
                RetrieverBlackboardKeys.RULE_ENGINE_MOCORE_OUTPUT_SYSTEM,
                RetrieverBlackboardKeys.RULE_ENGINE_MOCORE_OUTPUT_ALLOCATION,
                RetrieverBlackboardKeys.RULE_ENGINE_MOCORE_OUTPUT_RESOURCE_ENVIRONMENT));

        // Merge data & failure types into output repository
        super.add(new TypeMergerJob(getBlackboard(), RetrieverBlackboardKeys.RULE_ENGINE_BLACKBOARD_KEY_REPOSITORY,
                RetrieverBlackboardKeys.RULE_ENGINE_MOCORE_OUTPUT_REPOSITORY));

        // Persist repository, system, allocation, & resource environment model from blackboard into
        // file system
        super.add(new PersistenceJob(getBlackboard(), configuration.getInputFolder(), configuration.getOutputFolder(),
                RetrieverBlackboardKeys.RULE_ENGINE_MOCORE_OUTPUT_REPOSITORY,
                RetrieverBlackboardKeys.RULE_ENGINE_MOCORE_OUTPUT_SYSTEM,
                RetrieverBlackboardKeys.RULE_ENGINE_MOCORE_OUTPUT_ALLOCATION,
                RetrieverBlackboardKeys.RULE_ENGINE_MOCORE_OUTPUT_RESOURCE_ENVIRONMENT));

        super.add(new PlantUmlJob(configuration, getBlackboard()));
    }

    private List<ParallelJob> createRuleJobs(RetrieverConfiguration configuration) {
        List<ParallelJob> jobs = new ArrayList<>();

        for (Collection<Rule> step : configuration.getConfig(Rule.class)
            .getExecutionOrder()) {
            ParallelJob parentJob = new ParallelJob();
            for (Rule rule : step) {
                // Assume only build rules depend on build rules.
                if (rule.isBuildRule()) {
                    continue;
                }
                IBlackboardInteractingJob<RetrieverBlackboard> ruleJob = rule.create(configuration, myBlackboard);
                parentJob.add(ruleJob);
                logger.info("Adding rule job \"" + ruleJob.getName() + "\"");
            }
            jobs.add(parentJob);
        }

        return jobs;
    }

    private List<ParallelJob> createBuildRulesJob(RetrieverConfiguration configuration) {
        List<ParallelJob> jobs = new ArrayList<>();

        for (Collection<Rule> step : configuration.getConfig(Rule.class)
            .getExecutionOrder()) {
            ParallelJob parentJob = new ParallelJob();
            for (Rule rule : step) {
                // Assume only build rules depend on build rules.
                if (!rule.isBuildRule()) {
                    continue;
                }
                IBlackboardInteractingJob<RetrieverBlackboard> ruleJob = rule.create(configuration, myBlackboard);
                parentJob.add(ruleJob);
                logger.info("Adding build rule job \"" + ruleJob.getName() + "\"");
            }
            jobs.add(parentJob);
        }

        return jobs;
    }

    private List<ParallelJob> createDiscovererJobs(RetrieverConfiguration configuration) {
        List<ParallelJob> jobs = new ArrayList<>();

        for (Collection<Discoverer> step : configuration.getConfig(Discoverer.class)
            .getExecutionOrder()) {
            ParallelJob parentJob = new ParallelJob();
            for (Discoverer discoverer : step) {
                IBlackboardInteractingJob<RetrieverBlackboard> discovererJob = discoverer.create(configuration,
                        myBlackboard);
                parentJob.add(discovererJob);
                logger.info("Adding discoverer job \"" + discovererJob.getName() + "\"");
            }
            jobs.add(parentJob);
        }

        return jobs;
    }

    private List<ParallelJob> createAnalystJobs(RetrieverConfiguration configuration) {
        List<ParallelJob> jobs = new ArrayList<>();

        for (Collection<Analyst> step : configuration.getConfig(Analyst.class)
            .getExecutionOrder()) {
            ParallelJob parentJob = new ParallelJob();
            for (Analyst analyst : step) {
                IBlackboardInteractingJob<RetrieverBlackboard> analystJob = analyst.create(configuration,
                        myBlackboard);
                parentJob.add(analystJob);
                logger.info("Adding analyst job \"" + analystJob.getName() + "\"");
            }
            jobs.add(parentJob);
        }

        return jobs;
    }
}
