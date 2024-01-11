package org.palladiosimulator.retriever.mocore.transformation;

import java.util.List;

import org.palladiosimulator.generator.fluent.allocation.api.IAllocationAddition;
import org.palladiosimulator.generator.fluent.allocation.factory.FluentAllocationFactory;
import org.palladiosimulator.generator.fluent.allocation.structure.AllocationContextCreator;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.relation.ComponentAllocationRelation;

import tools.mdsd.mocore.framework.transformation.Transformer;

public class AllocationTransformer implements Transformer<PcmSurrogate, Allocation> {
    @Override
    public Allocation transform(PcmSurrogate model) {
        System system = new SystemTransformer().transform(model);
        ResourceEnvironment resourceEnvironment = new ResourceEnvironmentTransformer().transform(model);
        return this.transform(model, system, resourceEnvironment);
    }

    public Allocation transform(PcmSurrogate model, System system, ResourceEnvironment resourceEnvironment) {
        FluentAllocationFactory allocationFactory = new FluentAllocationFactory();
        IAllocationAddition fluentAllocation = allocationFactory.newAllocation()
                .withSystem(system)
                .withResourceEnvironment(resourceEnvironment);

        // Add allocation contexts to allocation
        List<ComponentAllocationRelation> relations = model.getByType(ComponentAllocationRelation.class);
        for (ComponentAllocationRelation relation : relations) {
            // Get and add context (creator) for specific allocation relation
            AllocationContextCreator contextCreator = getCreator(allocationFactory, relation);
            fluentAllocation.addToAllocation(contextCreator);
        }

        return fluentAllocation.createAllocationNow();
    }

    private AllocationContextCreator getCreator(FluentAllocationFactory fluentFactory,
            ComponentAllocationRelation relation) {
        AllocationContextCreator contextCreator = fluentFactory.newAllocationContext();

        // Use name of entities to fetch up-to-date entities from system and resource environment
        String assemblyContextName = SystemTransformer.getAssemblyContextName(relation.getSource());
        String deploymentEntityName = relation.getDestination().getValue().getEntityName();
        contextCreator.withAssemblyContext(assemblyContextName).withResourceContainer(deploymentEntityName);

        return contextCreator;
    }
}
