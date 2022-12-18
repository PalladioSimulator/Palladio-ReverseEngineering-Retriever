package org.palladiosimulator.somox.analyzer.rules.mocore.processor.element;

import java.util.List;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Component;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Interface;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.InterfaceProvisionRelation;

import com.gstuer.modelmerging.framework.processor.Processor;

public class InterfaceProcessor extends Processor<PcmSurrogate, Interface> {
    public InterfaceProcessor(PcmSurrogate model) {
        super(model, Interface.class);
    }

    @Override
    protected void refine(Interface discovery) {
        List<InterfaceProvisionRelation> providesRelations = getModel().getByType(InterfaceProvisionRelation.class);
        providesRelations.removeIf(relation -> !relation.getDestination().equals(discovery));
        List<InterfaceProvisionRelation> requiresRelations = getModel().getByType(InterfaceProvisionRelation.class);
        requiresRelations.removeIf(relation -> !relation.getDestination().equals(discovery));

        if (providesRelations.isEmpty() && requiresRelations.isEmpty()) {
            Component component = Component.getUniquePlaceholder();
            InterfaceProvisionRelation relation = new InterfaceProvisionRelation(component, discovery, true);
            addImplication(relation);
        }
    }

}
