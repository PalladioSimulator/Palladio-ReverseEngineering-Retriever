package org.palladiosimulator.retriever.mocore.processor.relation;

import java.util.List;

import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.Interface;
import org.palladiosimulator.retriever.mocore.surrogate.relation.ComponentAssemblyRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.InterfaceProvisionRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.InterfaceRequirementRelation;

import tools.mdsd.mocore.framework.processor.RelationProcessor;

public class InterfaceProvisionRelationProcessor extends RelationProcessor<PcmSurrogate, InterfaceProvisionRelation> {
    public InterfaceProvisionRelationProcessor(final PcmSurrogate model) {
        super(model, InterfaceProvisionRelation.class);
    }

    @Override
    protected void refine(final InterfaceProvisionRelation discovery) {
        final Interface commonInterface = discovery.getDestination();

        // Get all requirements from model & filter for same interface as in discovery
        final List<InterfaceRequirementRelation> requirementRelations = this.getModel()
            .getByType(InterfaceRequirementRelation.class);
        requirementRelations.removeIf(relation -> !relation.getDestination()
            .equals(commonInterface));

        // Create component assembly placeholder for pairs of provision & requirement relations
        for (final InterfaceRequirementRelation requirementRelation : requirementRelations) {
            final ComponentAssemblyRelation assemblyRelation = new ComponentAssemblyRelation(discovery,
                    requirementRelation, true);
            this.addImplication(assemblyRelation);
        }

        super.refine(discovery);
    }
}
