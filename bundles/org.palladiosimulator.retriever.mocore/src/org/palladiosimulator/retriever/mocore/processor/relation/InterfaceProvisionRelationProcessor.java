package org.palladiosimulator.retriever.mocore.processor.relation;

import java.util.List;

import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.Interface;
import org.palladiosimulator.retriever.mocore.surrogate.relation.ComponentAssemblyRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.InterfaceProvisionRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.InterfaceRequirementRelation;

import tools.mdsd.mocore.framework.processor.RelationProcessor;

public class InterfaceProvisionRelationProcessor extends RelationProcessor<PcmSurrogate, InterfaceProvisionRelation> {
    public InterfaceProvisionRelationProcessor(PcmSurrogate model) {
        super(model, InterfaceProvisionRelation.class);
    }

    @Override
    protected void refine(InterfaceProvisionRelation discovery) {
        Interface commonInterface = discovery.getDestination();

        // Get all requirements from model & filter for same interface as in discovery
        List<InterfaceRequirementRelation> requirementRelations = this.getModel()
            .getByType(InterfaceRequirementRelation.class);
        requirementRelations.removeIf(relation -> !relation.getDestination()
            .equals(commonInterface));

        // Create component assembly placeholder for pairs of provision & requirement relations
        for (InterfaceRequirementRelation requirementRelation : requirementRelations) {
            ComponentAssemblyRelation assemblyRelation = new ComponentAssemblyRelation(discovery, requirementRelation,
                    true);
            this.addImplication(assemblyRelation);
        }

        super.refine(discovery);
    }
}
