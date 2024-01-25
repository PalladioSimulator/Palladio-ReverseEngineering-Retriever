package org.palladiosimulator.retriever.core.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.palladiosimulator.retriever.core.configuration.RetrieverBlackboardKeys;
import org.palladiosimulator.retriever.core.service.Analyst;
import org.palladiosimulator.retriever.extraction.blackboard.RetrieverBlackboard;
import org.palladiosimulator.retriever.extraction.engine.Discoverer;
import org.palladiosimulator.retriever.extraction.engine.RetrieverConfiguration;
import org.palladiosimulator.retriever.extraction.engine.Rule;
import org.palladiosimulator.retriever.mocore.workflow.MoCoReJob;
import org.palladiosimulator.somox.ast2seff.jobs.Ast2SeffJob;

import de.uka.ipd.sdq.workflow.extension.AbstractExtendableJob;
import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.ParallelJob;

public class RetrieverJob extends AbstractExtendableJob<RetrieverBlackboard> {

    public RetrieverJob(final RetrieverConfiguration configuration) {
        super.setBlackboard(new RetrieverBlackboard());

        super.addAll(this.createDiscovererJobs(configuration));

        super.addAll(this.createRuleJobs(configuration));

        super.addAll(this.createBuildRulesJob(configuration));

        super.add(new RetrieverBlackboardInteractingJob(configuration, this.getBlackboard()));

        super.addAll(this.createAnalystJobs(configuration));

        // Generate service effect specifications based on AST nodes and merge them into repository
        super.add(new Ast2SeffJob(this.getBlackboard(),
                RetrieverBlackboardKeys.RULE_ENGINE_BLACKBOARD_KEY_SEFF_ASSOCIATIONS,
                RetrieverBlackboardKeys.RULE_ENGINE_AST2SEFF_OUTPUT_REPOSITORY));
        super.add(new SeffMergerJob(this.myBlackboard, RetrieverBlackboardKeys.RULE_ENGINE_AST2SEFF_OUTPUT_REPOSITORY,
                RetrieverBlackboardKeys.RULE_ENGINE_BLACKBOARD_KEY_REPOSITORY));

        // Refine model and create final repository, system, allocation, & resource environment
        super.add(new MoCoReJob(this.getBlackboard(), RetrieverBlackboardKeys.RULE_ENGINE_BLACKBOARD_KEY_REPOSITORY,
                RetrieverBlackboardKeys.RULE_ENGINE_MOCORE_OUTPUT_REPOSITORY,
                RetrieverBlackboardKeys.RULE_ENGINE_MOCORE_OUTPUT_SYSTEM,
                RetrieverBlackboardKeys.RULE_ENGINE_MOCORE_OUTPUT_ALLOCATION,
                RetrieverBlackboardKeys.RULE_ENGINE_MOCORE_OUTPUT_RESOURCE_ENVIRONMENT));

        // Merge data & failure types into output repository
        super.add(new TypeMergerJob(this.getBlackboard(), RetrieverBlackboardKeys.RULE_ENGINE_BLACKBOARD_KEY_REPOSITORY,
                RetrieverBlackboardKeys.RULE_ENGINE_MOCORE_OUTPUT_REPOSITORY));

        // Persist repository, system, allocation, & resource environment model from blackboard into
        // file system
        super.add(new PersistenceJob(this.getBlackboard(), configuration.getInputFolder(),
                configuration.getOutputFolder(), RetrieverBlackboardKeys.RULE_ENGINE_MOCORE_OUTPUT_REPOSITORY,
                RetrieverBlackboardKeys.RULE_ENGINE_MOCORE_OUTPUT_SYSTEM,
                RetrieverBlackboardKeys.RULE_ENGINE_MOCORE_OUTPUT_ALLOCATION,
                RetrieverBlackboardKeys.RULE_ENGINE_MOCORE_OUTPUT_RESOURCE_ENVIRONMENT));

        super.add(new PlantUmlJob(getBlackboard(), configuration.getOutputFolder(),
                RetrieverBlackboardKeys.RULE_ENGINE_MOCORE_OUTPUT_REPOSITORY,
                RetrieverBlackboardKeys.RULE_ENGINE_MOCORE_OUTPUT_SYSTEM,
                RetrieverBlackboardKeys.RULE_ENGINE_MOCORE_OUTPUT_ALLOCATION));
    }

    private List<ParallelJob> createRuleJobs(final RetrieverConfiguration configuration) {
        final List<ParallelJob> jobs = new ArrayList<>();

        for (final Collection<Rule> step : configuration.getConfig(Rule.class)
            .getExecutionOrder()) {
            final ParallelJob parentJob = new ParallelJob();
            for (final Rule rule : step) {
                // Assume only build rules depend on build rules.
                if (rule.isBuildRule()) {
                    continue;
                }
                final IBlackboardInteractingJob<RetrieverBlackboard> ruleJob = rule.create(configuration,
                        this.myBlackboard);
                parentJob.add(ruleJob);
                this.logger.info("Adding rule job \"" + ruleJob.getName() + "\"");
            }
            jobs.add(parentJob);
        }

        return jobs;
    }

    private List<ParallelJob> createBuildRulesJob(final RetrieverConfiguration configuration) {
        final List<ParallelJob> jobs = new ArrayList<>();

        for (final Collection<Rule> step : configuration.getConfig(Rule.class)
            .getExecutionOrder()) {
            final ParallelJob parentJob = new ParallelJob();
            for (final Rule rule : step) {
                // Assume only build rules depend on build rules.
                if (!rule.isBuildRule()) {
                    continue;
                }
                final IBlackboardInteractingJob<RetrieverBlackboard> ruleJob = rule.create(configuration,
                        this.myBlackboard);
                parentJob.add(ruleJob);
                this.logger.info("Adding build rule job \"" + ruleJob.getName() + "\"");
            }
            jobs.add(parentJob);
        }

        return jobs;
    }

    private List<ParallelJob> createDiscovererJobs(final RetrieverConfiguration configuration) {
        final List<ParallelJob> jobs = new ArrayList<>();

        for (final Collection<Discoverer> step : configuration.getConfig(Discoverer.class)
            .getExecutionOrder()) {
            final ParallelJob parentJob = new ParallelJob();
            for (final Discoverer discoverer : step) {
                final IBlackboardInteractingJob<RetrieverBlackboard> discovererJob = discoverer.create(configuration,
                        this.myBlackboard);
                parentJob.add(discovererJob);
                this.logger.info("Adding discoverer job \"" + discovererJob.getName() + "\"");
            }
            jobs.add(parentJob);
        }

        return jobs;
    }

    private List<ParallelJob> createAnalystJobs(final RetrieverConfiguration configuration) {
        final List<ParallelJob> jobs = new ArrayList<>();

        for (final Collection<Analyst> step : configuration.getConfig(Analyst.class)
            .getExecutionOrder()) {
            final ParallelJob parentJob = new ParallelJob();
            for (final Analyst analyst : step) {
                final IBlackboardInteractingJob<RetrieverBlackboard> analystJob = analyst.create(configuration,
                        this.myBlackboard);
                parentJob.add(analystJob);
                this.logger.info("Adding analyst job \"" + analystJob.getName() + "\"");
            }
            jobs.add(parentJob);
        }

        return jobs;
    }
}
