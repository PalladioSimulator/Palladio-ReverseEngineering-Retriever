package org.palladiosimulator.somox.analyzer.rules.workflow;

import org.somox.gast2seff.jobs.GAST2SEFFJob;
import org.somox.gast2seff.visitors.IFunctionClassificationStrategyFactory;
import org.somox.gast2seff.visitors.InterfaceOfExternalCallFindingFactory;

public class SeffCreatorJob extends GAST2SEFFJob {

    // TODO integration SEFF extraction
    public SeffCreatorJob(final boolean createResourceDemandingInternalBehaviour,
            final IFunctionClassificationStrategyFactory iFunctionClassificationStrategyFactory,
            final InterfaceOfExternalCallFindingFactory interfaceOfExternalCallFindingFactory) {
        super(createResourceDemandingInternalBehaviour, iFunctionClassificationStrategyFactory,
                interfaceOfExternalCallFindingFactory);
    }

}
