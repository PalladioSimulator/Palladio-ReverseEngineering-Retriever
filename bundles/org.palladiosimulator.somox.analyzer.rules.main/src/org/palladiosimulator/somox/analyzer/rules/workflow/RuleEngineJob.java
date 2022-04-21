package org.palladiosimulator.somox.analyzer.rules.workflow;

import org.eclipse.core.runtime.CoreException;
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineConfiguration;

import de.uka.ipd.sdq.workflow.extension.AbstractExtendableJob;
import de.uka.ipd.sdq.workflow.jobs.AbstractJob;
import de.uka.ipd.sdq.workflow.jobs.ParallelJob;

public class RuleEngineJob extends AbstractExtendableJob<RuleEngineBlackboard> {

    public RuleEngineJob(RuleEngineConfiguration configuration) throws CoreException {
        setBlackboard(new RuleEngineBlackboard());

        this.add(discoveryJobs(configuration));

        this.add(new RuleEngineBlackboardInteractingJob(configuration, getBlackboard()));

        // TODO integration SEFF extraction
        // this.add(new SeffCreatorJob(false, null, null));

        this.add(new ModelSaverJob(configuration));

        addAnalysts(configuration);
    }

    private ParallelJob discoveryJobs(RuleEngineConfiguration configuration) {
        ParallelJob discoveryJobs = new ParallelJob();

        // FIXME this throws XML parsing errors when run on more complex projects (e.g. acmeair)
        // discoveryJobs.add(new JdtParserJob(configuration, getBlackboard()));

        return discoveryJobs;
    }

    private void addAnalysts(RuleEngineConfiguration configuration) throws CoreException {
        for (Analyst analyst : configuration.getSelectedAnalysts()) {
            AbstractJob analystJob = analyst.create(configuration, myBlackboard);
            this.add(analystJob);
            logger.info("Adding analyst job \"" + analystJob.getName() + "\"");
        }
    }
}
