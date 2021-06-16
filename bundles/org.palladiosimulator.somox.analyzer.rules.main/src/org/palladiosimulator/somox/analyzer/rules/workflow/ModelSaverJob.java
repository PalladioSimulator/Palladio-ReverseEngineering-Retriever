package org.palladiosimulator.somox.analyzer.rules.workflow;

import org.somox.configuration.AbstractMoxConfiguration;
import org.somox.gast2seff.jobs.SaveSoMoXModelsJob;

public class ModelSaverJob extends SaveSoMoXModelsJob {

    // TODO extract saving from RuleEngine to this job
    public ModelSaverJob(AbstractMoxConfiguration somoxConfiguration) {
        super(somoxConfiguration);
    }

}
