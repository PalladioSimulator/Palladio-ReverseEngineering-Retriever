package com.gstuer.modelmerging.instance.pcm.utility;

import org.palladiosimulator.generator.fluent.repository.factory.FluentRepositoryFactory;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.RepositoryFactory;
import org.palladiosimulator.pcm.resourceenvironment.CommunicationLinkResourceSpecification;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentFactory;

import com.gstuer.modelmerging.instance.pcm.surrogate.element.Component;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Deployment;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Interface;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.LinkResourceSpecification;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.ServiceEffectSpecification;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Signature;
import com.gstuer.modelmerging.utility.IdentifierGenerator;

public final class ElementFactory {
    private ElementFactory() {
        throw new IllegalStateException("Cannot instantiate utility class.");
    }

    public static Signature createUniqueSignature(boolean isPlaceholder) {
        String identifier = IdentifierGenerator.getUniqueIdentifier();
        OperationSignature value = RepositoryFactory.eINSTANCE.createOperationSignature();
        value.setEntityName(identifier);
        return new Signature(value, isPlaceholder);
    }

    public static Interface createUniqueInterface(boolean isPlaceholder) {
        String identifier = IdentifierGenerator.getUniqueIdentifier();
        OperationInterface value = new FluentRepositoryFactory().newOperationInterface().withName(identifier).build();
        return new Interface(value, isPlaceholder);
    }

    public static Component createUniqueComponent(boolean isPlaceholder) {
        String identifier = IdentifierGenerator.getUniqueIdentifier();
        BasicComponent value = new FluentRepositoryFactory().newBasicComponent().withName(identifier).build();
        return new Component(value, isPlaceholder);
    }

    public static Deployment createUniqueDeployment(boolean isPlaceholder) {
        String identifier = IdentifierGenerator.getUniqueIdentifier();
        ResourceContainer value = ResourceenvironmentFactory.eINSTANCE.createResourceContainer();
        value.setEntityName(identifier);
        return new Deployment(value, isPlaceholder);
    }

    public static LinkResourceSpecification createUniqueLinkResourceSpecification(boolean isPlaceholder) {
        String identifier = IdentifierGenerator.getUniqueIdentifier();
        CommunicationLinkResourceSpecification value = ResourceenvironmentFactory.eINSTANCE
                .createCommunicationLinkResourceSpecification();
        value.setId(identifier);
        return new LinkResourceSpecification(value, isPlaceholder);
    }

    public static ServiceEffectSpecification createUniqueServiceEffectSpecification(boolean isPlaceholder) {
        ServiceEffectSpecification placeholderSpecification = ServiceEffectSpecification.getUniquePlaceholder();
        return new ServiceEffectSpecification(placeholderSpecification.getValue(), isPlaceholder);
    }
}
