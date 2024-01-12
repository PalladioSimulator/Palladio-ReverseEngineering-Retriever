package org.palladiosimulator.retriever.mocore.processor.relation;

import java.util.List;

import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.Interface;
import org.palladiosimulator.retriever.mocore.surrogate.relation.ComponentAssemblyRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.InterfaceProvisionRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.InterfaceRequirementRelation;

import tools.mdsd.mocore.framework.processor.RelationProcessor;

public class InterfaceRequirementRelationProcessor
        extends RelationProcessor<PcmSurrogate, InterfaceRequirementRelation> {
    public InterfaceRequirementRelationProcessor(PcmSurrogate model) {
        super(model, InterfaceRequirementRelation.class);
    }

    @Override
    protected void refine(InterfaceRequirementRelation discovery) {
        Interface commonInterface = discovery.getDestination();

        // Get all requirements from model & filter for same interface as in discovery
        List<InterfaceProvisionRelation> provisionRelations = this.getModel()
            .getByType(InterfaceProvisionRelation.class);
        provisionRelations.removeIf(relation -> !relation.getDestination()
            .equals(commonInterface));

        // Create component assembly placeholder for pairs of provision & requirement relations
        for (InterfaceProvisionRelation provisionRelation : provisionRelations) {
            ComponentAssemblyRelation assemblyRelation = new ComponentAssemblyRelation(provisionRelation, discovery,
                    true);
            this.addImplication(assemblyRelation);
        }

        super.refine(discovery);
    }
}
