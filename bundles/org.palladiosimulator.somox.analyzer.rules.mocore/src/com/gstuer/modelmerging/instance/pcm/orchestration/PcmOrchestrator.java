package com.gstuer.modelmerging.instance.pcm.orchestration;

import com.gstuer.modelmerging.framework.orchestration.Orchestrator;
import com.gstuer.modelmerging.instance.pcm.processor.element.ComponentProcessor;
import com.gstuer.modelmerging.instance.pcm.processor.element.DeploymentProcessor;
import com.gstuer.modelmerging.instance.pcm.processor.element.InterfaceProcessor;
import com.gstuer.modelmerging.instance.pcm.processor.element.LinkResourceSpecificationProcessor;
import com.gstuer.modelmerging.instance.pcm.processor.element.ServiceEffectSpecificationProcessor;
import com.gstuer.modelmerging.instance.pcm.processor.element.SignatureProcessor;
import com.gstuer.modelmerging.instance.pcm.processor.relation.ComponentAllocationRelationProcessor;
import com.gstuer.modelmerging.instance.pcm.processor.relation.ComponentAssemblyRelationProcessor;
import com.gstuer.modelmerging.instance.pcm.processor.relation.ComponentSignatureProvisionRelationProcessor;
import com.gstuer.modelmerging.instance.pcm.processor.relation.DeploymentDeploymentRelationProcessor;
import com.gstuer.modelmerging.instance.pcm.processor.relation.InterfaceProvisionRelationProcessor;
import com.gstuer.modelmerging.instance.pcm.processor.relation.InterfaceRequirementRelationProcessor;
import com.gstuer.modelmerging.instance.pcm.processor.relation.LinkResourceSpecificationRelationProcessor;
import com.gstuer.modelmerging.instance.pcm.processor.relation.ServiceEffectSpecificationRelationProcessor;
import com.gstuer.modelmerging.instance.pcm.processor.relation.SignatureProvisionRelationProcessor;
import com.gstuer.modelmerging.instance.pcm.surrogate.PcmSurrogate;

public class PcmOrchestrator extends Orchestrator<PcmSurrogate> {
    public PcmOrchestrator(PcmSurrogate model) {
        super(model, new SignatureProcessor(model), new InterfaceProcessor(model), new ComponentProcessor(model),
                new DeploymentProcessor(model), new LinkResourceSpecificationProcessor(model),
                new ServiceEffectSpecificationProcessor(model), new SignatureProvisionRelationProcessor(model),
                new InterfaceProvisionRelationProcessor(model), new InterfaceRequirementRelationProcessor(model),
                new ComponentAssemblyRelationProcessor(model), new ComponentAllocationRelationProcessor(model),
                new DeploymentDeploymentRelationProcessor(model), new LinkResourceSpecificationRelationProcessor(model),
                new ServiceEffectSpecificationRelationProcessor(model),
                new ComponentSignatureProvisionRelationProcessor(model));
    }

    public PcmOrchestrator() {
        this(new PcmSurrogate());
    }
}
