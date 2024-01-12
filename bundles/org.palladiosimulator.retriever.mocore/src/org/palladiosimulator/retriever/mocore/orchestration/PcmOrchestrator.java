package org.palladiosimulator.retriever.mocore.orchestration;

import org.palladiosimulator.retriever.mocore.processor.element.AtomicComponentProcessor;
import org.palladiosimulator.retriever.mocore.processor.element.CompositeProcessor;
import org.palladiosimulator.retriever.mocore.processor.element.DeploymentProcessor;
import org.palladiosimulator.retriever.mocore.processor.element.InterfaceProcessor;
import org.palladiosimulator.retriever.mocore.processor.element.LinkResourceSpecificationProcessor;
import org.palladiosimulator.retriever.mocore.processor.element.ServiceEffectSpecificationProcessor;
import org.palladiosimulator.retriever.mocore.processor.element.SignatureProcessor;
import org.palladiosimulator.retriever.mocore.processor.relation.ComponentAllocationRelationProcessor;
import org.palladiosimulator.retriever.mocore.processor.relation.ComponentAssemblyRelationProcessor;
import org.palladiosimulator.retriever.mocore.processor.relation.ComponentSignatureProvisionRelationProcessor;
import org.palladiosimulator.retriever.mocore.processor.relation.CompositeProvisionDelegationRelationProcessor;
import org.palladiosimulator.retriever.mocore.processor.relation.CompositeRequirementDelegationRelationProcessor;
import org.palladiosimulator.retriever.mocore.processor.relation.CompositionRelationProcessor;
import org.palladiosimulator.retriever.mocore.processor.relation.DeploymentDeploymentRelationProcessor;
import org.palladiosimulator.retriever.mocore.processor.relation.InterfaceProvisionRelationProcessor;
import org.palladiosimulator.retriever.mocore.processor.relation.InterfaceRequirementRelationProcessor;
import org.palladiosimulator.retriever.mocore.processor.relation.LinkResourceSpecificationRelationProcessor;
import org.palladiosimulator.retriever.mocore.processor.relation.ServiceEffectSpecificationRelationProcessor;
import org.palladiosimulator.retriever.mocore.processor.relation.SignatureProvisionRelationProcessor;
import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;

import tools.mdsd.mocore.framework.orchestration.Orchestrator;

public class PcmOrchestrator extends Orchestrator<PcmSurrogate> {
    public PcmOrchestrator(PcmSurrogate model) {
        super(model, new SignatureProcessor(model), new InterfaceProcessor(model), new DeploymentProcessor(model),
                new LinkResourceSpecificationProcessor(model), new ServiceEffectSpecificationProcessor(model),
                new SignatureProvisionRelationProcessor(model), new InterfaceProvisionRelationProcessor(model),
                new InterfaceRequirementRelationProcessor(model), new ComponentAssemblyRelationProcessor(model),
                new ComponentAllocationRelationProcessor(model), new DeploymentDeploymentRelationProcessor(model),
                new LinkResourceSpecificationRelationProcessor(model),
                new ServiceEffectSpecificationRelationProcessor(model), new AtomicComponentProcessor(model),
                new ComponentSignatureProvisionRelationProcessor(model), new CompositeProcessor(model),
                new CompositionRelationProcessor(model), new CompositeRequirementDelegationRelationProcessor(model),
                new CompositeProvisionDelegationRelationProcessor(model));
    }

    public PcmOrchestrator() {
        this(new PcmSurrogate());
    }
}
