package org.palladiosimulator.somox.analyzer.rules.mocore.utility;

import org.palladiosimulator.generator.fluent.repository.factory.FluentRepositoryFactory;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.RepositoryFactory;
import org.palladiosimulator.pcm.resourceenvironment.CommunicationLinkResourceSpecification;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentFactory;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.AtomicComponent;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Component;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Deployment;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Interface;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.LinkResourceSpecification;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.ServiceEffectSpecification;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Signature;

import tools.mdsd.mocore.utility.IdentifierGenerator;

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

    public static Component<?> createUniqueComponent(boolean isPlaceholder) {
        String identifier = IdentifierGenerator.getUniqueIdentifier();
        BasicComponent value = new FluentRepositoryFactory().newBasicComponent().withName(identifier).build();
        return new AtomicComponent(value, isPlaceholder);
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
