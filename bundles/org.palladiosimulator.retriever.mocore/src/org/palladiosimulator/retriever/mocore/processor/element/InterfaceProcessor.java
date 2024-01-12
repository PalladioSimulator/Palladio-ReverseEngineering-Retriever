package org.palladiosimulator.retriever.mocore.processor.element;

import java.util.List;

import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.Component;
import org.palladiosimulator.retriever.mocore.surrogate.element.Interface;
import org.palladiosimulator.retriever.mocore.surrogate.relation.InterfaceProvisionRelation;

import tools.mdsd.mocore.framework.processor.Processor;

public class InterfaceProcessor extends Processor<PcmSurrogate, Interface> {
    private static final String PLACEHOLDER_COMPONENT_NAME_PATTERN = "%s Provider";

    public InterfaceProcessor(final PcmSurrogate model) {
        super(model, Interface.class);
    }

    @Override
    protected void refine(final Interface discovery) {
        final List<InterfaceProvisionRelation> providesRelations = this.getModel()
            .getByType(InterfaceProvisionRelation.class);
        providesRelations.removeIf(relation -> !relation.getDestination()
            .equals(discovery));

        // Rule: Each interface has to be provided by a component.
        // -> If no provision relation exists yet, add a placeholder provider and relation to the
        // model.
        if (providesRelations.isEmpty()) {
            final String interfaceName = discovery.getValue()
                .getEntityName();
            final String componentName = String.format(PLACEHOLDER_COMPONENT_NAME_PATTERN, interfaceName);
            final Component<?> component = Component.getNamedPlaceholder(componentName);
            final InterfaceProvisionRelation relation = new InterfaceProvisionRelation(component, discovery, true);
            this.addImplication(relation);
        }
    }

}
