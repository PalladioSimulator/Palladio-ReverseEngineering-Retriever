package org.palladiosimulator.somox.analyzer.rules.workflow;

import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineAnalyzerConfiguration;

import de.uka.ipd.sdq.workflow.extension.AbstractExtendableJob;
import de.uka.ipd.sdq.workflow.jobs.ParallelJob;

public class RuleEngineJob extends AbstractExtendableJob<RuleEngineBlackboard> {

    public RuleEngineJob(RuleEngineAnalyzerConfiguration configuration) {
        setBlackboard(new RuleEngineBlackboard());

        this.add(discoveryJobs(configuration));
        
        this.add(new RuleEngineBlackboardInteractingJob(configuration, getBlackboard()));

        // TODO integration SEFF extraction
        // this.add(new SeffCreatorJob(false, null, null));

        this.add(new ModelSaverJob(configuration.getMoxConfiguration()));
        


        
        configuration.getMoxConfiguration().setInputFolder(configuration.getMoxConfiguration().getInputFolder());
    }

    
    
    private ParallelJob discoveryJobs(RuleEngineAnalyzerConfiguration configuration) {
    	ParallelJob discoveryJobs = new ParallelJob();
    	
    	discoveryJobs.add(new JdtParserJob(configuration, getBlackboard()));
    	// TODO Add Job for Docker discovery
    	
		return discoveryJobs;
    }

    
}
