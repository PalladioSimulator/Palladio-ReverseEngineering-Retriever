package org.palladiosimulator.somox.analyzer.rules.mocore.transformation;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.EList;
import org.palladiosimulator.generator.fluent.repository.api.Repo;
import org.palladiosimulator.generator.fluent.repository.factory.FluentRepositoryFactory;
import org.palladiosimulator.generator.fluent.repository.structure.components.BasicComponentCreator;
import org.palladiosimulator.generator.fluent.repository.structure.components.CompositeComponentCreator;
import org.palladiosimulator.generator.fluent.repository.structure.interfaces.OperationInterfaceCreator;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.CompositeComponent;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationRequiredRole;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryComponent;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.ExternalCallAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.ServiceEffectSpecification;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.AtomicComponent;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Component;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Composite;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Interface;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Signature;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.CompositionRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.InterfaceProvisionRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.InterfaceRequirementRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.ServiceEffectSpecificationRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.SignatureProvisionRelation;

import tools.mdsd.mocore.framework.transformation.Transformer;

public class RepositoryTransformer implements Transformer<PcmSurrogate, Repository> {
    private static final String ROLE_PROVIDES_NAME_PATTERN = "%s Provider";
    private static final String ROLE_REQUIRES_NAME_PATTERN = "%s Consumer";

    @Override
    public Repository transform(PcmSurrogate model) {
        FluentRepositoryFactory repositoryFactory = new FluentRepositoryFactory();
        Repo fluentRepository = repositoryFactory.newRepository();

        List<ServiceEffectSpecificationRelation> seffRelations = model
                .getByType(ServiceEffectSpecificationRelation.class);
        List<InterfaceProvisionRelation> provisionRelations = model.getByType(InterfaceProvisionRelation.class);
        List<InterfaceRequirementRelation> requirementRelations = model.getByType(InterfaceRequirementRelation.class);
        List<SignatureProvisionRelation> signatureRelations = model.getByType(SignatureProvisionRelation.class);
        List<Interface> interfaces = model.getByType(Interface.class);

        // Add interfaces to fluent repository
        for (Interface interfaceInstance : interfaces) {
            OperationInterfaceCreator interfaceCreator = getCreator(repositoryFactory, interfaceInstance);

            // Add interface to repository and fetch built interface
            fluentRepository.addToRepository(interfaceCreator);
            OperationInterface repositoryInterface = repositoryFactory
                    .fetchOfOperationInterface(interfaceInstance.getValue().getEntityName());

            // Add signatures to the added interface directly
            // Avoids the creation of signature creator and tight coupling to fluentApi
            for (SignatureProvisionRelation relation : signatureRelations) {
                if (relation.getDestination().equals(interfaceInstance)) {
                    Signature signature = relation.getSource();
                    signature.getValue().setInterface__OperationSignature(repositoryInterface);
                }
            }
        }

        // Add basic components with their roles and seff to fluent repository
        for (AtomicComponent component : model.getByType(AtomicComponent.class)) {
            BasicComponentCreator componentCreator = getCreator(repositoryFactory, component);

            // Add provided interfaces
            for (InterfaceProvisionRelation relation : provisionRelations) {
                Interface interfaceInstance = relation.getDestination();
                if (relation.getSource().equals(component)) {
                    String interfaceName = interfaceInstance.getValue().getEntityName();
                    OperationInterface operationInterface = repositoryFactory.fetchOfOperationInterface(interfaceName);
                    componentCreator.provides(operationInterface, getProvidedRoleName(interfaceInstance));
                }
            }

            // Add required interfaces
            for (InterfaceRequirementRelation relation : requirementRelations) {
                Interface interfaceInstance = relation.getDestination();
                if (relation.getSource().equals(component)) {
                    String interfaceName = interfaceInstance.getValue().getEntityName();
                    OperationInterface operationInterface = repositoryFactory.fetchOfOperationInterface(interfaceName);
                    componentCreator.requires(operationInterface, getRequiredRoleName(interfaceInstance));
                }
            }

            // Build component to make changes that are unsupported by fluent api
            BasicComponent repositoryComponent = componentCreator.build();

            // Add service effect specifications to component
            // For each provided interface, iterate over each signature of interface and add seff if it exists
            for (InterfaceProvisionRelation interfaceProvision : provisionRelations) {
                if (interfaceProvision.getSource().equals(component)) {
                    OperationInterface operationInterface = repositoryFactory
                            .fetchOfOperationInterface(interfaceProvision.getDestination().getValue().getEntityName());
                    for (OperationSignature signature : operationInterface.getSignatures__OperationInterface()) {
                        // Get seff entity for specific signature in interface
                        Predicate<ServiceEffectSpecificationRelation> filter = relation -> {
                            final Signature wrappedSignature = relation.getSource().getDestination().getSource();
                            final Interface wrappedInterface = relation.getSource().getSource().getDestination();
                            return representSameSignature(signature, wrappedSignature.getValue())
                                    && representSameInterface(operationInterface, wrappedInterface.getValue());
                        };
                        ServiceEffectSpecification seff = seffRelations.stream()
                                .filter(relation -> relation.getSource().getSource().getSource().equals(component))
                                .filter(filter)
                                .map(relation -> relation.getDestination().getValue()).findFirst()
                                .orElse(org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.ServiceEffectSpecification
                                        .getUniquePlaceholder().getValue());

                        // Reset component and signature within seff because they may be out-dated
                        seff.setBasicComponent_ServiceEffectSpecification(repositoryComponent);
                        seff.setDescribedService__SEFF(signature);

                        // Fix changed identifier of required roles in external call actions
                        if (seff instanceof ResourceDemandingSEFF) {
                            ResourceDemandingSEFF rdSeff = (ResourceDemandingSEFF) seff;
                            EList<AbstractAction> behavior = rdSeff.getSteps_Behaviour();
                            List<ExternalCallAction> externalCallActions = behavior.stream()
                                    .filter(action -> action instanceof ExternalCallAction)
                                    .map(action -> (ExternalCallAction) action)
                                    .collect(Collectors.toList());

                            for (ExternalCallAction externalCallAction : externalCallActions) {
                                OperationSignature externalSignature = externalCallAction
                                        .getCalledService_ExternalService();

                                // Get required role containing called signature of externalCallAction from component
                                OperationRequiredRole requiredRole = repositoryComponent
                                        .getRequiredRoles_InterfaceRequiringEntity().stream()
                                        .filter(role -> role instanceof OperationRequiredRole)
                                        .map(role -> (OperationRequiredRole) role)
                                        .filter(role -> role.getRequiredInterface__OperationRequiredRole()
                                                .getSignatures__OperationInterface().contains(externalSignature))
                                        .findFirst().orElseThrow();

                                // Set role in external call action to fetched required role
                                externalCallAction.setRole_ExternalService(requiredRole);
                            }
                        }
                    }
                }
            }

            // Add created basic component with roles and seff to repository
            fluentRepository.addToRepository(repositoryComponent);
        }

        // Add composite components with their roles to fluent repository
        for (Composite composite : model.getByType(Composite.class)) {
            CompositeComponentCreator compositeCreator = getCreator(repositoryFactory, composite);

            // Add provided interfaces
            for (InterfaceProvisionRelation relation : provisionRelations) {
                Interface interfaceInstance = relation.getDestination();
                if (relation.getSource().equals(composite)) {
                    String interfaceName = interfaceInstance.getValue().getEntityName();
                    OperationInterface operationInterface = repositoryFactory.fetchOfOperationInterface(interfaceName);
                    compositeCreator.provides(operationInterface, getProvidedRoleName(interfaceInstance));
                }
            }

            // Add required interfaces
            for (InterfaceRequirementRelation relation : requirementRelations) {
                Interface interfaceInstance = relation.getDestination();
                if (relation.getSource().equals(composite)) {
                    String interfaceName = interfaceInstance.getValue().getEntityName();
                    OperationInterface operationInterface = repositoryFactory.fetchOfOperationInterface(interfaceName);
                    compositeCreator.requires(operationInterface, getRequiredRoleName(interfaceInstance));
                }
            }

            // Add composite to fluent repository
            fluentRepository.addToRepository(compositeCreator);
        }

        // Add compositions to repository -> All composites & composites have to be added beforehand
        List<CompositionRelation> compositionRelations = model.getByType(CompositionRelation.class);
        for (CompositionRelation relation : compositionRelations) {
            Composite composite = relation.getSource();
            Component<?> destination = relation.getDestination();

            // Fetch composite from repository
            CompositeComponent persistedCompositeComponent = repositoryFactory
                    .fetchOfCompositeComponent(composite.getValue().getEntityName());
            persistedCompositeComponent.getAssemblyContexts__ComposedStructure();

            // Fetch composite child from repository & create temporary fluent creator
            CompositeComponentCreator temporaryCreator = repositoryFactory.newCompositeComponent();
            if (destination instanceof AtomicComponent) {
                temporaryCreator.withAssemblyContext(
                        repositoryFactory.fetchOfBasicComponent(destination.getValue().getEntityName()));
            } else if (destination instanceof Composite) {
                temporaryCreator.withAssemblyContext(
                        repositoryFactory.fetchOfCompositeComponent(destination.getValue().getEntityName()));
            }
            CompositeComponent temporaryComposite = (CompositeComponent) temporaryCreator.build();

            // Copy assembly contexts from temporary to persisted composite
            persistedCompositeComponent.getAssemblyContexts__ComposedStructure()
                    .addAll(temporaryComposite.getAssemblyContexts__ComposedStructure());
        }

        // TODO Add composite delegations

        return fluentRepository.createRepositoryNow();
    }

    private BasicComponentCreator getCreator(FluentRepositoryFactory fluentFactory, AtomicComponent component) {
        BasicComponentCreator componentCreator = fluentFactory.newBasicComponent();

        // TODO Identify important information within wrapped component
        // Copy information from wrapped component, dismiss deprecated information.
        BasicComponent wrappedComponent = component.getValue();
        componentCreator.withName(wrappedComponent.getEntityName());

        return componentCreator;
    }

    private CompositeComponentCreator getCreator(FluentRepositoryFactory fluentFactory, Composite component) {
        CompositeComponentCreator compositeCreator = fluentFactory.newCompositeComponent();

        // TODO Identify important information within wrapped component
        // Copy information from wrapped component, dismiss deprecated information.
        RepositoryComponent wrappedComponent = component.getValue();
        compositeCreator.withName(wrappedComponent.getEntityName());

        return compositeCreator;
    }

    private OperationInterfaceCreator getCreator(FluentRepositoryFactory fluentFactory, Interface interfaceInstance) {
        OperationInterfaceCreator interfaceCreator = fluentFactory.newOperationInterface();

        // TODO Identify important information within wrapped interface
        // Copy information from wrapped interface, dismiss deprecated information.
        OperationInterface wrappedInterface = interfaceInstance.getValue();
        interfaceCreator.withName(wrappedInterface.getEntityName());

        return interfaceCreator;
    }

    protected static String getProvidedRoleName(Interface interfaceInstance) {
        String interfaceEntityName = interfaceInstance.getValue().getEntityName();
        return String.format(ROLE_PROVIDES_NAME_PATTERN, interfaceEntityName);
    }

    protected static String getRequiredRoleName(Interface interfaceInstance) {
        String interfaceEntityName = interfaceInstance.getValue().getEntityName();
        return String.format(ROLE_REQUIRES_NAME_PATTERN, interfaceEntityName);
    }

    // TODO Test and move to evaluation helper
    private static boolean representSameSignature(OperationSignature signature, OperationSignature otherSignature) {
        boolean equalName = Objects.equals(signature.getEntityName(), otherSignature.getEntityName());
        boolean equalReturn = Objects.equals(signature.getReturnType__OperationSignature(),
                otherSignature.getReturnType__OperationSignature());
        boolean equalParameters = signature.getParameters__OperationSignature()
                .containsAll(otherSignature.getParameters__OperationSignature())
                && otherSignature.getParameters__OperationSignature()
                        .containsAll(signature.getParameters__OperationSignature());
        return equalName && equalReturn && equalParameters;
    }

    // TODO Test and move to evaluation helper
    private static boolean representSameInterface(OperationInterface interFace, OperationInterface otherInterFace) {
        boolean equalName = Objects.equals(interFace.getEntityName(), otherInterFace.getEntityName());
        // TODO Check if signatures are equal via representSameSignature
        return equalName;
    }
}