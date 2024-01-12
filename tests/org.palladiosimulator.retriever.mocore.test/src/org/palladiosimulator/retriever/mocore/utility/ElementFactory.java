package org.palladiosimulator.retriever.mocore.utility;

import org.palladiosimulator.generator.fluent.repository.factory.FluentRepositoryFactory;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.CompositeComponent;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.RepositoryComponent;
import org.palladiosimulator.pcm.repository.RepositoryFactory;
import org.palladiosimulator.pcm.resourceenvironment.CommunicationLinkResourceSpecification;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentFactory;
import org.palladiosimulator.retriever.mocore.surrogate.element.AtomicComponent;
import org.palladiosimulator.retriever.mocore.surrogate.element.Component;
import org.palladiosimulator.retriever.mocore.surrogate.element.Composite;
import org.palladiosimulator.retriever.mocore.surrogate.element.Deployment;
import org.palladiosimulator.retriever.mocore.surrogate.element.Interface;
import org.palladiosimulator.retriever.mocore.surrogate.element.LinkResourceSpecification;
import org.palladiosimulator.retriever.mocore.surrogate.element.ServiceEffectSpecification;
import org.palladiosimulator.retriever.mocore.surrogate.element.Signature;

import tools.mdsd.mocore.utility.IdentifierGenerator;

public final class ElementFactory {
    private ElementFactory() {
        throw new IllegalStateException("Cannot instantiate utility class.");
    }

    public static Signature createUniqueSignature(final boolean isPlaceholder) {
        final String identifier = IdentifierGenerator.getUniqueIdentifier();
        final OperationSignature value = RepositoryFactory.eINSTANCE.createOperationSignature();
        value.setEntityName(identifier);
        return new Signature(value, isPlaceholder);
    }

    public static Interface createUniqueInterface(final boolean isPlaceholder) {
        final String identifier = IdentifierGenerator.getUniqueIdentifier();
        final OperationInterface value = new FluentRepositoryFactory().newOperationInterface()
            .withName(identifier)
            .build();
        return new Interface(value, isPlaceholder);
    }

    public static Component<?> createUniqueComponent(final boolean isPlaceholder) {
        final String identifier = IdentifierGenerator.getUniqueIdentifier();
        final BasicComponent value = new FluentRepositoryFactory().newBasicComponent()
            .withName(identifier)
            .build();
        return new AtomicComponent(value, isPlaceholder);
    }

    public static Composite createUniqueComposite(final boolean isPlaceholder) {
        final String identifier = IdentifierGenerator.getUniqueIdentifier();
        final RepositoryComponent value = new FluentRepositoryFactory().newCompositeComponent()
            .withName(identifier)
            .build();
        return new Composite((CompositeComponent) value, isPlaceholder);
    }

    public static Deployment createUniqueDeployment(final boolean isPlaceholder) {
        final String identifier = IdentifierGenerator.getUniqueIdentifier();
        final ResourceContainer value = ResourceenvironmentFactory.eINSTANCE.createResourceContainer();
        value.setEntityName(identifier);
        return new Deployment(value, isPlaceholder);
    }

    public static LinkResourceSpecification createUniqueLinkResourceSpecification(final boolean isPlaceholder) {
        final String identifier = IdentifierGenerator.getUniqueIdentifier();
        final CommunicationLinkResourceSpecification value = ResourceenvironmentFactory.eINSTANCE
            .createCommunicationLinkResourceSpecification();
        value.setId(identifier);
        return new LinkResourceSpecification(value, isPlaceholder);
    }

    public static ServiceEffectSpecification createUniqueServiceEffectSpecification(final boolean isPlaceholder) {
        final ServiceEffectSpecification placeholderSpecification = ServiceEffectSpecification.getUniquePlaceholder();
        return new ServiceEffectSpecification(placeholderSpecification.getValue(), isPlaceholder);
    }
}
