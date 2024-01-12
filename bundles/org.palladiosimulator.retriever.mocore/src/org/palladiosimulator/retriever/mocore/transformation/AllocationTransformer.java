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
    public Allocation transform(final PcmSurrogate model) {
        final System system = new SystemTransformer().transform(model);
        final ResourceEnvironment resourceEnvironment = new ResourceEnvironmentTransformer().transform(model);
        return this.transform(model, system, resourceEnvironment);
    }

    public Allocation transform(final PcmSurrogate model, final System system,
            final ResourceEnvironment resourceEnvironment) {
        final FluentAllocationFactory allocationFactory = new FluentAllocationFactory();
        final IAllocationAddition fluentAllocation = allocationFactory.newAllocation()
            .withSystem(system)
            .withResourceEnvironment(resourceEnvironment);

        // Add allocation contexts to allocation
        final List<ComponentAllocationRelation> relations = model.getByType(ComponentAllocationRelation.class);
        for (final ComponentAllocationRelation relation : relations) {
            // Get and add context (creator) for specific allocation relation
            final AllocationContextCreator contextCreator = this.getCreator(allocationFactory, relation);
            fluentAllocation.addToAllocation(contextCreator);
        }

        return fluentAllocation.createAllocationNow();
    }

    private AllocationContextCreator getCreator(final FluentAllocationFactory fluentFactory,
            final ComponentAllocationRelation relation) {
        final AllocationContextCreator contextCreator = fluentFactory.newAllocationContext();

        // Use name of entities to fetch up-to-date entities from system and resource environment
        final String assemblyContextName = SystemTransformer.getAssemblyContextName(relation.getSource());
        final String deploymentEntityName = relation.getDestination()
            .getValue()
            .getEntityName();
        contextCreator.withAssemblyContext(assemblyContextName)
            .withResourceContainer(deploymentEntityName);

        return contextCreator;
    }
}
