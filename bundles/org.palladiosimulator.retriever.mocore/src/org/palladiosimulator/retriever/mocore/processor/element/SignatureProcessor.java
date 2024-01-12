package org.palladiosimulator.retriever.mocore.processor.element;

import java.util.List;

import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.Interface;
import org.palladiosimulator.retriever.mocore.surrogate.element.Signature;
import org.palladiosimulator.retriever.mocore.surrogate.relation.SignatureProvisionRelation;

import tools.mdsd.mocore.framework.processor.Processor;

public class SignatureProcessor extends Processor<PcmSurrogate, Signature> {
    public SignatureProcessor(PcmSurrogate model) {
        super(model, Signature.class);
    }

    @Override
    protected void refine(Signature discovery) {
        // Add providing interface for signature if none exists
        List<SignatureProvisionRelation> interfaceRelations = getModel().getByType(SignatureProvisionRelation.class);
        interfaceRelations.removeIf(relation -> !relation.getSource()
            .equals(discovery));
        if (interfaceRelations.isEmpty()) {
            Interface interfaceElement = Interface.getUniquePlaceholder();
            SignatureProvisionRelation relation = new SignatureProvisionRelation(discovery, interfaceElement, true);
            addImplication(relation);
        }
    }
}
