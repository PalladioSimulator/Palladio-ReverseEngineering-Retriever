package org.palladiosimulator.somox.analyzer.rules.mocore.orchestration;

import org.palladiosimulator.somox.analyzer.rules.mocore.processor.element.AtomicComponentProcessor;
import org.palladiosimulator.somox.analyzer.rules.mocore.processor.element.CompositeProcessor;
import org.palladiosimulator.somox.analyzer.rules.mocore.processor.element.DeploymentProcessor;
import org.palladiosimulator.somox.analyzer.rules.mocore.processor.element.InterfaceProcessor;
import org.palladiosimulator.somox.analyzer.rules.mocore.processor.element.LinkResourceSpecificationProcessor;
import org.palladiosimulator.somox.analyzer.rules.mocore.processor.element.ServiceEffectSpecificationProcessor;
import org.palladiosimulator.somox.analyzer.rules.mocore.processor.element.SignatureProcessor;
import org.palladiosimulator.somox.analyzer.rules.mocore.processor.relation.ComponentAllocationRelationProcessor;
import org.palladiosimulator.somox.analyzer.rules.mocore.processor.relation.ComponentAssemblyRelationProcessor;
import org.palladiosimulator.somox.analyzer.rules.mocore.processor.relation.ComponentSignatureProvisionRelationProcessor;
import org.palladiosimulator.somox.analyzer.rules.mocore.processor.relation.CompositeProvisionDelegationRelationProcessor;
import org.palladiosimulator.somox.analyzer.rules.mocore.processor.relation.CompositeRequirementDelegationRelationProcessor;
import org.palladiosimulator.somox.analyzer.rules.mocore.processor.relation.CompositionRelationProcessor;
import org.palladiosimulator.somox.analyzer.rules.mocore.processor.relation.DeploymentDeploymentRelationProcessor;
import org.palladiosimulator.somox.analyzer.rules.mocore.processor.relation.InterfaceProvisionRelationProcessor;
import org.palladiosimulator.somox.analyzer.rules.mocore.processor.relation.InterfaceRequirementRelationProcessor;
import org.palladiosimulator.somox.analyzer.rules.mocore.processor.relation.LinkResourceSpecificationRelationProcessor;
import org.palladiosimulator.somox.analyzer.rules.mocore.processor.relation.ServiceEffectSpecificationRelationProcessor;
import org.palladiosimulator.somox.analyzer.rules.mocore.processor.relation.SignatureProvisionRelationProcessor;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;

import tools.mdsd.mocore.framework.orchestration.Orchestrator;

public class PcmOrchestrator extends Orchestrator<PcmSurrogate> {
    public PcmOrchestrator(PcmSurrogate model) {
        super(model, new SignatureProcessor(model), new InterfaceProcessor(model),
                new DeploymentProcessor(model), new LinkResourceSpecificationProcessor(model),
                new ServiceEffectSpecificationProcessor(model), new SignatureProvisionRelationProcessor(model),
                new InterfaceProvisionRelationProcessor(model), new InterfaceRequirementRelationProcessor(model),
                new ComponentAssemblyRelationProcessor(model), new ComponentAllocationRelationProcessor(model),
                new DeploymentDeploymentRelationProcessor(model), new LinkResourceSpecificationRelationProcessor(model),
                new ServiceEffectSpecificationRelationProcessor(model), new AtomicComponentProcessor(model),
                new ComponentSignatureProvisionRelationProcessor(model), new CompositeProcessor(model),
                new CompositionRelationProcessor(model), new CompositeRequirementDelegationRelationProcessor(model),
                new CompositeProvisionDelegationRelationProcessor(model));
    }

    public PcmOrchestrator() {
        this(new PcmSurrogate());
    }
}
