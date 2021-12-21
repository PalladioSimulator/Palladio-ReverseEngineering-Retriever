package org.palladiosimulator.somox.analyzer.rules.workflow;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineConfiguration;

import de.uka.ipd.sdq.workflow.extension.AbstractExtendableJob;
import de.uka.ipd.sdq.workflow.jobs.ParallelJob;

public class RuleEngineJob extends AbstractExtendableJob<RuleEngineBlackboard> {

    private static final String ANALYST_EXTENSION_POINT = "org.palladiosimulator.somox.analyzer.rules.analyst";

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

        discoveryJobs.add(new JdtParserJob(configuration, getBlackboard()));
        // TODO Add Job for Docker discovery

        return discoveryJobs;
    }
    
    private void addAnalysts(RuleEngineConfiguration configuration) throws CoreException {
        for (IConfigurationElement extension : Platform.getExtensionRegistry()
            .getConfigurationElementsFor(ANALYST_EXTENSION_POINT)) {
            final Object o = extension.createExecutableExtension("class");
            if (o instanceof Analyst) {
                logger.info(String.format("Adding analyst %s", extension.getDeclaringExtension().getUniqueIdentifier()));
                this.add(((Analyst) o).create(configuration, getBlackboard()));
            }
        }
    }
}
