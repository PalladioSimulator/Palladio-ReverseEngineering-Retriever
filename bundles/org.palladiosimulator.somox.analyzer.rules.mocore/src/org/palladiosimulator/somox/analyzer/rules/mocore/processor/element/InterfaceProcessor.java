package org.palladiosimulator.somox.analyzer.rules.mocore.processor.element;

import java.util.List;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Component;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Interface;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.InterfaceProvisionRelation;

import tools.mdsd.mocore.framework.processor.Processor;

public class InterfaceProcessor extends Processor<PcmSurrogate, Interface> {
    public InterfaceProcessor(PcmSurrogate model) {
        super(model, Interface.class);
    }

    @Override
    protected void refine(Interface discovery) {
        List<InterfaceProvisionRelation> providesRelations = getModel().getByType(InterfaceProvisionRelation.class);
        providesRelations.removeIf(relation -> !relation.getDestination().equals(discovery));

        // Rule: Each interface has to be provided by a component.
        // -> If no provision relation exists yet, add a placeholder provider and relation to the model.
        if (providesRelations.isEmpty()) {
            Component<?> component = Component.getUniquePlaceholder();
            InterfaceProvisionRelation relation = new InterfaceProvisionRelation(component, discovery, true);
            addImplication(relation);
        }
    }

}
