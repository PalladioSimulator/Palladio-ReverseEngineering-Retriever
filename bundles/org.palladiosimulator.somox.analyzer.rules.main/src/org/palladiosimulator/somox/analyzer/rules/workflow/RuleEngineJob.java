package org.palladiosimulator.somox.analyzer.rules.workflow;

import org.eclipse.core.runtime.CoreException;
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineConfiguration;
import org.palladiosimulator.somox.analyzer.rules.service.Analyst;
import org.palladiosimulator.somox.discoverer.Discoverer;

import de.uka.ipd.sdq.workflow.extension.AbstractExtendableJob;
import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.ParallelJob;

public class RuleEngineJob extends AbstractExtendableJob<RuleEngineBlackboard> {

    public RuleEngineJob(RuleEngineConfiguration configuration) throws CoreException {
        setBlackboard(new RuleEngineBlackboard());

        this.add(createDiscoverersJob(configuration));

        this.add(new RuleEngineBlackboardInteractingJob(configuration, getBlackboard()));

        // TODO integration SEFF extraction
        // this.add(new SeffCreatorJob(false, null, null));

        this.add(new ModelSaverJob(configuration));

        this.add(createAnalystsJob(configuration));
    }

    private ParallelJob createDiscoverersJob(RuleEngineConfiguration configuration) throws CoreException {
        ParallelJob parentJob = new ParallelJob();
        for (Discoverer discoverer : configuration.getDiscovererConfig()
            .getSelected()) {
            IBlackboardInteractingJob<RuleEngineBlackboard> discovererJob = discoverer.create(configuration, myBlackboard);
            parentJob.add(discovererJob);
            logger.info("Adding discoverer job \"" + discovererJob.getName() + "\"");
        }
        return parentJob;
    }

    private ParallelJob createAnalystsJob(RuleEngineConfiguration configuration) throws CoreException {
        ParallelJob parentJob = new ParallelJob();
        for (Analyst analyst : configuration.getAnalystConfig()
            .getSelected()) {
            IBlackboardInteractingJob<RuleEngineBlackboard> analystJob = analyst.create(configuration, myBlackboard);
            parentJob.add(analystJob);
            logger.info("Adding analyst job \"" + analystJob.getName() + "\"");
        }
        return parentJob;
    }
}
