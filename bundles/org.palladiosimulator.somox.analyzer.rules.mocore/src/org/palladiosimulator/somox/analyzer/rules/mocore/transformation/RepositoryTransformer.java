package org.palladiosimulator.somox.analyzer.rules.mocore.transformation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.palladiosimulator.generator.fluent.repository.api.Repo;
import org.palladiosimulator.generator.fluent.repository.factory.FluentRepositoryFactory;
import org.palladiosimulator.generator.fluent.repository.structure.components.BasicComponentCreator;
import org.palladiosimulator.generator.fluent.repository.structure.components.CompositeComponentCreator;
import org.palladiosimulator.generator.fluent.repository.structure.interfaces.OperationInterfaceCreator;
import org.palladiosimulator.pcm.core.composition.AssemblyConnector;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.composition.CompositionFactory;
import org.palladiosimulator.pcm.core.composition.ProvidedDelegationConnector;
import org.palladiosimulator.pcm.core.composition.RequiredDelegationConnector;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.CompositeComponent;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
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
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.ComponentAssemblyRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.CompositeProvisionDelegationRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.CompositeRequirementDelegationRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.CompositionRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.InterfaceProvisionRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.InterfaceRequirementRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.ServiceEffectSpecificationRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.SignatureProvisionRelation;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import tools.mdsd.mocore.framework.transformation.Transformer;

public class RepositoryTransformer implements Transformer<PcmSurrogate, Repository> {
    private static final Logger LOG = Logger.getLogger(RepositoryTransformer.class);

    private static final String DELEGATION_EXCLUSION_NAME_PATTERN = "%s";
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
                    .fetchOfOperationInterface(interfaceInstance.getValue()
                            .getEntityName());

            // Add signatures to the added interface directly
            // Avoids the creation of signature creator and tight coupling to fluentApi
            for (SignatureProvisionRelation relation : signatureRelations) {
                if (relation.getDestination()
                        .equals(interfaceInstance)) {
                    Signature signature = relation.getSource();
                    signature.getValue()
                            .setInterface__OperationSignature(repositoryInterface);
                }
            }
        }

        // Add basic components with their roles and seff to fluent repository
        for (AtomicComponent component : model.getByType(AtomicComponent.class)) {
            BasicComponentCreator componentCreator = getCreator(repositoryFactory, component);

            // Add provided interfaces
            for (InterfaceProvisionRelation relation : provisionRelations) {
                Interface interfaceInstance = relation.getDestination();
                if (relation.getSource()
                        .equals(component)) {
                    String interfaceName = interfaceInstance.getValue()
                            .getEntityName();
                    OperationInterface operationInterface = repositoryFactory.fetchOfOperationInterface(interfaceName);
                    componentCreator.provides(operationInterface, getProvidedRoleName(interfaceInstance));
                }
            }

            // Add required interfaces
            for (InterfaceRequirementRelation relation : requirementRelations) {
                Interface interfaceInstance = relation.getDestination();
                if (relation.getSource()
                        .equals(component)) {
                    String interfaceName = interfaceInstance.getValue()
                            .getEntityName();
                    OperationInterface operationInterface = repositoryFactory.fetchOfOperationInterface(interfaceName);
                    componentCreator.requires(operationInterface, getRequiredRoleName(interfaceInstance));
                }
            }

            // Build component to make changes that are unsupported by fluent api
            BasicComponent repositoryComponent = componentCreator.build();

            // Add service effect specifications to component
            // For each provided interface, iterate over each signature of interface and add seff if
            // it exists
            for (InterfaceProvisionRelation interfaceProvision : provisionRelations) {
                if (interfaceProvision.getSource()
                        .equals(component)) {
                    OperationInterface operationInterface = repositoryFactory
                            .fetchOfOperationInterface(interfaceProvision.getDestination()
                                    .getValue()
                                    .getEntityName());
                    for (OperationSignature signature : operationInterface.getSignatures__OperationInterface()) {
                        // Get seff entity for specific signature in interface
                        Predicate<ServiceEffectSpecificationRelation> filter = relation -> {
                            final Signature wrappedSignature = relation.getSource()
                                    .getDestination()
                                    .getSource();
                            final Interface wrappedInterface = relation.getSource()
                                    .getSource()
                                    .getDestination();
                            return representSameSignature(signature, wrappedSignature.getValue())
                                    && representSameInterface(operationInterface, wrappedInterface.getValue());
                        };
                        ServiceEffectSpecification seff = seffRelations.stream()
                                .filter(relation -> relation.getSource()
                                        .getSource()
                                        .getSource()
                                        .equals(component))
                                .filter(filter)
                                .map(relation -> relation.getDestination()
                                        .getValue())
                                .findFirst()
                                .orElse(org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.ServiceEffectSpecification
                                        .getUniquePlaceholder()
                                        .getValue());

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

                                // Get required role containing called signature of
                                // externalCallAction from component
                                Optional<OperationRequiredRole> requiredRoleOption = repositoryComponent
                                        .getRequiredRoles_InterfaceRequiringEntity()
                                        .stream()
                                        .filter(role -> role instanceof OperationRequiredRole)
                                        .map(role -> (OperationRequiredRole) role)
                                        .filter(role -> role.getRequiredInterface__OperationRequiredRole()
                                                .getSignatures__OperationInterface()
                                                .contains(externalSignature))
                                        .findFirst();
                                if (requiredRoleOption.isEmpty()) {
                                    LOG.warn(
                                            "Failed to find required role for external call to "
                                                    + externalSignature.getInterface__OperationSignature()
                                                            .getEntityName()
                                                    + "#" + externalSignature.getEntityName() + "!");
                                    continue;
                                }
                                OperationRequiredRole requiredRole = requiredRoleOption.get();

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

        // Add implicitly provided interfaces of composites to model (non-required interface of child).
        // Problem: This has to happen from innermost to outermost component. -> Sorted by dependency.
        // First step: Get children of each composite
        List<CompositionRelation> compositionRelations = model.getByType(CompositionRelation.class);
        List<Composite> composites = model.getByType(Composite.class);
        Multimap<Composite, Component<?>> compositesChildren = HashMultimap.create();
        for (Composite composite : composites) {
            List<Component<?>> children = compositionRelations.stream()
                    .filter(relation -> relation.getSource().equals(composite))
                    .map(relation -> relation.getDestination())
                    .collect(Collectors.toList());
            compositesChildren.putAll(composite, children);
        }
        // Second step: Sort composites
        List<Composite> sortedComposites = new LinkedList<>(composites);
        sortedComposites.sort((a, b) -> compareComposites(a, b, compositesChildren));
        // Third step: Get non-required interfaces & their providers
        List<InterfaceProvisionRelation> nonRequiredProvisionRelations = new LinkedList<>(provisionRelations);
        nonRequiredProvisionRelations.removeIf(
                provisionRelation -> requirementRelations.stream().anyMatch(requirementRelation -> requirementRelation
                        .getDestination().equals(provisionRelation.getDestination())));
        // Fourth step: Provide non-required interface of children & add delegation
        for (Composite composite : sortedComposites) {
            for (int i = 0; i < nonRequiredProvisionRelations.size(); i++) {
                // Access via index due to concurrent modification -> New last element might be added to list
                InterfaceProvisionRelation nonRequiredProvision = nonRequiredProvisionRelations.get(i);
                Component<?> provider = nonRequiredProvision.getSource();
                Interface providedInterface = nonRequiredProvision.getDestination();

                // Only add if provider is direct child of composite
                if (isDirectChild(provider, composite, compositesChildren)) {
                    // Check whether delegation already exists in model
                    boolean existsDelegation = false;
                    for (CompositeProvisionDelegationRelation delegationRelation : model
                            .getByType(CompositeProvisionDelegationRelation.class)) {
                        if (delegationRelation.getDestination().equals(nonRequiredProvision)
                                && delegationRelation.getSource().getSource().equals(composite)) {
                            existsDelegation = true;
                            break;
                        }
                    }

                    // Check whether interface should be excluded from recursive delegation
                    boolean excludeDelegation = isExcludedFromDelegation(provider, providedInterface);

                    if (!existsDelegation && !excludeDelegation) {
                        // Check whether interface provision already exists
                        InterfaceProvisionRelation provisionRelation = null;
                        for (InterfaceProvisionRelation provision : provisionRelations) {
                            if (provision.getSource().equals(composite)
                                    && provision.getDestination().equals(providedInterface)) {
                                provisionRelation = provision;
                            }
                        }
                        // Create new provision if it does not exist yet
                        if (Objects.isNull(provisionRelation)) {
                            provisionRelation = new InterfaceProvisionRelation(composite,
                                    providedInterface, true);

                            // Add to model and refresh already fetched relations
                            model.add(provisionRelation);
                            nonRequiredProvisionRelations.add(provisionRelation);
                            provisionRelations = model.getByType(InterfaceProvisionRelation.class);
                        }

                        // Add provided delegation connector
                        CompositeProvisionDelegationRelation provisionDelegation = new CompositeProvisionDelegationRelation(
                                provisionRelation, nonRequiredProvision, true);
                        model.add(provisionDelegation);
                    }
                }
            }
        }

        // Add composite components with their roles to fluent repository
        for (Composite composite : composites) {
            CompositeComponentCreator compositeCreator = getCreator(repositoryFactory, composite);

            // Add explicitly provided interfaces
            for (InterfaceProvisionRelation relation : provisionRelations) {
                Interface interfaceInstance = relation.getDestination();
                if (relation.getSource()
                        .equals(composite)) {
                    String interfaceName = interfaceInstance.getValue()
                            .getEntityName();
                    OperationInterface operationInterface = repositoryFactory.fetchOfOperationInterface(interfaceName);
                    compositeCreator.provides(operationInterface, getProvidedRoleName(interfaceInstance));
                }
            }

            // Add required interfaces
            for (InterfaceRequirementRelation relation : requirementRelations) {
                Interface interfaceInstance = relation.getDestination();
                if (relation.getSource()
                        .equals(composite)) {
                    String interfaceName = interfaceInstance.getValue()
                            .getEntityName();
                    OperationInterface operationInterface = repositoryFactory.fetchOfOperationInterface(interfaceName);
                    compositeCreator.requires(operationInterface, getRequiredRoleName(interfaceInstance));
                }
            }

            // Add composite to fluent repository
            fluentRepository.addToRepository(compositeCreator);
        }

        // Add compositions to repository -> All composites & composites have to be added beforehand
        for (CompositionRelation relation : compositionRelations) {
            Composite composite = relation.getSource();
            Component<?> destination = relation.getDestination();

            // Fetch composite from repository
            CompositeComponent persistedCompositeComponent = repositoryFactory
                    .fetchOfCompositeComponent(composite.getValue()
                            .getEntityName());
            persistedCompositeComponent.getAssemblyContexts__ComposedStructure();

            // Fetch composite child from repository & create temporary fluent creator
            CompositeComponentCreator temporaryCreator = repositoryFactory.newCompositeComponent();
            if (destination instanceof AtomicComponent) {
                temporaryCreator.withAssemblyContext(repositoryFactory.fetchOfBasicComponent(destination.getValue()
                        .getEntityName()));
            } else if (destination instanceof Composite) {
                temporaryCreator.withAssemblyContext(repositoryFactory.fetchOfCompositeComponent(destination.getValue()
                        .getEntityName()));
            }

            // Copy assembly contexts from temporary to persisted composite
            CompositeComponent temporaryComposite = (CompositeComponent) temporaryCreator.build();
            persistedCompositeComponent.getAssemblyContexts__ComposedStructure()
                    .addAll(temporaryComposite.getAssemblyContexts__ComposedStructure());
        }

        Repository repository = fluentRepository.createRepositoryNow();

        // Add assembly connectors for assembly relations of components within same composite
        for (ComponentAssemblyRelation assemblyRelation : model.getByType(ComponentAssemblyRelation.class)) {
            Component<?> provider = assemblyRelation.getSource().getSource();
            Component<?> consumer = assemblyRelation.getDestination().getSource();
            Interface interFace = assemblyRelation.getSource().getDestination();

            // Get common composites of provider and consumer
            List<Composite> providerComposites = compositionRelations.stream()
                    .filter(relation -> relation.getDestination().equals(provider)).map(CompositionRelation::getSource)
                    .collect(Collectors.toList());
            List<Composite> consumerComposites = compositionRelations.stream()
                    .filter(relation -> relation.getDestination().equals(consumer)).map(CompositionRelation::getSource)
                    .collect(Collectors.toList());
            List<Composite> commonComposites = providerComposites.stream()
                    .filter(composite -> consumerComposites.contains(composite)).collect(Collectors.toList());

            // Get real composites of wrappers from repository
            List<CompositeComponent> commonRepositoryComposites = new ArrayList<>();
            for (RepositoryComponent repositoryComponent : repository.getComponents__Repository()) {
                for (Composite commonComposite : commonComposites) {
                    if (repositoryComponent.getEntityName().equals(commonComposite.getValue().getEntityName())) {
                        commonRepositoryComposites.add((CompositeComponent) repositoryComponent);
                    }
                }
            }

            // Add assembly connector to each common composite
            for (CompositeComponent repositoryComposite : commonRepositoryComposites) {
                // Fetch assembly contexts from composite
                AssemblyContext providerContext = repositoryComposite.getAssemblyContexts__ComposedStructure().stream()
                        .filter(context -> context.getEncapsulatedComponent__AssemblyContext().getEntityName()
                                .equals(provider.getValue().getEntityName()))
                        .findFirst().orElseThrow();
                AssemblyContext consumerContext = repositoryComposite.getAssemblyContexts__ComposedStructure().stream()
                        .filter(context -> context.getEncapsulatedComponent__AssemblyContext().getEntityName()
                                .equals(consumer.getValue().getEntityName()))
                        .findFirst().orElseThrow();

                // Fetch roles from contexts
                OperationProvidedRole providerRole = (OperationProvidedRole) providerContext
                        .getEncapsulatedComponent__AssemblyContext()
                        .getProvidedRoles_InterfaceProvidingEntity().stream()
                        .filter(role -> role instanceof OperationProvidedRole
                                && ((OperationProvidedRole) role).getProvidedInterface__OperationProvidedRole()
                                        .getEntityName().equals(interFace.getValue().getEntityName()))
                        .findFirst().orElseThrow();
                OperationRequiredRole consumerRole = (OperationRequiredRole) consumerContext
                        .getEncapsulatedComponent__AssemblyContext()
                        .getRequiredRoles_InterfaceRequiringEntity().stream()
                        .filter(role -> role instanceof OperationRequiredRole
                                && ((OperationRequiredRole) role).getRequiredInterface__OperationRequiredRole()
                                        .getEntityName().equals(interFace.getValue().getEntityName()))
                        .findFirst().orElseThrow();

                // Construct assembly connector
                AssemblyConnector assemblyConnector = CompositionFactory.eINSTANCE.createAssemblyConnector();
                assemblyConnector.setProvidedRole_AssemblyConnector(providerRole);
                assemblyConnector.setProvidingAssemblyContext_AssemblyConnector(providerContext);
                assemblyConnector.setRequiredRole_AssemblyConnector(consumerRole);
                assemblyConnector.setRequiringAssemblyContext_AssemblyConnector(consumerContext);

                // Add connector to composite
                repositoryComposite.getConnectors__ComposedStructure().add(assemblyConnector);
            }
        }

        // Add provided delegation connectors to composite
        for (CompositeProvisionDelegationRelation delegationRelation : model
                .getByType(CompositeProvisionDelegationRelation.class)) {
            // Decompose delegation relation into components & interfaces
            Composite compositeWrapper = (Composite) delegationRelation.getSource().getSource();
            Component<?> childWrapper = delegationRelation.getDestination().getSource();
            Interface outerInterfaceWrapper = delegationRelation.getSource().getDestination();
            Interface innerInterfaceWrapper = delegationRelation.getDestination().getDestination();

            // Fetch composite, assembly context, & roles from repository
            CompositeComponent repositoryComposite = (CompositeComponent) repository.getComponents__Repository()
                    .stream()
                    .filter(CompositeComponent.class::isInstance)
                    .filter(component -> component.getEntityName().equals(compositeWrapper.getValue().getEntityName()))
                    .findFirst().orElseThrow();
            AssemblyContext childContext = repositoryComposite.getAssemblyContexts__ComposedStructure().stream()
                    .filter(context -> context.getEncapsulatedComponent__AssemblyContext().getEntityName()
                            .equals(childWrapper.getValue().getEntityName()))
                    .findFirst().orElseThrow();
            OperationProvidedRole innerRole = (OperationProvidedRole) childContext
                    .getEncapsulatedComponent__AssemblyContext()
                    .getProvidedRoles_InterfaceProvidingEntity().stream()
                    .filter(role -> role instanceof OperationProvidedRole
                            && ((OperationProvidedRole) role).getProvidedInterface__OperationProvidedRole()
                                    .getEntityName().equals(innerInterfaceWrapper.getValue().getEntityName()))
                    .findFirst().orElseThrow();
            OperationProvidedRole outerRole = (OperationProvidedRole) repositoryComposite
                    .getProvidedRoles_InterfaceProvidingEntity().stream()
                    .filter(role -> role instanceof OperationProvidedRole
                            && ((OperationProvidedRole) role).getProvidedInterface__OperationProvidedRole()
                                    .getEntityName().equals(outerInterfaceWrapper.getValue().getEntityName()))
                    .findFirst().orElseThrow();

            // Create delegation connector
            ProvidedDelegationConnector delegationConnector = CompositionFactory.eINSTANCE
                    .createProvidedDelegationConnector();
            delegationConnector.setAssemblyContext_ProvidedDelegationConnector(childContext);
            delegationConnector.setInnerProvidedRole_ProvidedDelegationConnector(innerRole);
            delegationConnector.setOuterProvidedRole_ProvidedDelegationConnector(outerRole);

            // Add connector to composite component
            repositoryComposite.getConnectors__ComposedStructure().add(delegationConnector);
        }

        // Add required delegation connectors to composite
        for (CompositeRequirementDelegationRelation delegationRelation : model
                .getByType(CompositeRequirementDelegationRelation.class)) {
            // Decompose delegation relation into components & interfaces
            Composite compositeWrapper = (Composite) delegationRelation.getSource().getSource();
            Component<?> childWrapper = delegationRelation.getDestination().getSource();
            Interface outerInterfaceWrapper = delegationRelation.getSource().getDestination();
            Interface innerInterfaceWrapper = delegationRelation.getDestination().getDestination();

            // Fetch composite, assembly context, & roles from repository
            CompositeComponent repositoryComposite = (CompositeComponent) repository.getComponents__Repository()
                    .stream()
                    .filter(component -> component.getEntityName().equals(compositeWrapper.getValue().getEntityName()))
                    .findFirst().orElseThrow();
            AssemblyContext childContext = repositoryComposite.getAssemblyContexts__ComposedStructure().stream()
                    .filter(context -> context.getEncapsulatedComponent__AssemblyContext().getEntityName()
                            .equals(childWrapper.getValue().getEntityName()))
                    .findFirst().orElseThrow();
            OperationRequiredRole innerRole = (OperationRequiredRole) childContext
                    .getEncapsulatedComponent__AssemblyContext()
                    .getRequiredRoles_InterfaceRequiringEntity().stream()
                    .filter(role -> role instanceof OperationRequiredRole
                            && ((OperationRequiredRole) role).getRequiredInterface__OperationRequiredRole()
                                    .getEntityName().equals(innerInterfaceWrapper.getValue().getEntityName()))
                    .findFirst().orElseThrow();
            OperationRequiredRole outerRole = (OperationRequiredRole) repositoryComposite
                    .getRequiredRoles_InterfaceRequiringEntity().stream()
                    .filter(role -> role instanceof OperationRequiredRole
                            && ((OperationRequiredRole) role).getRequiredInterface__OperationRequiredRole()
                                    .getEntityName().equals(outerInterfaceWrapper.getValue().getEntityName()))
                    .findFirst().orElseThrow();

            // Create delegation connector
            RequiredDelegationConnector delegationConnector = CompositionFactory.eINSTANCE
                    .createRequiredDelegationConnector();
            delegationConnector.setAssemblyContext_RequiredDelegationConnector(childContext);
            delegationConnector.setInnerRequiredRole_RequiredDelegationConnector(innerRole);
            delegationConnector.setOuterRequiredRole_RequiredDelegationConnector(outerRole);

            // Add connector to composite component
            repositoryComposite.getConnectors__ComposedStructure().add(delegationConnector);
        }

        return repository;

    }

    private boolean isDirectChild(Component<?> child, Composite parent,
            Multimap<Composite, Component<?>> compositesChildren) {
        return compositesChildren.get(parent).contains(child);
    }

    private boolean isRecursiveChild(Component<?> child, Composite parent,
            Multimap<Composite, Component<?>> compositesChildren) {
        // Case 1: Direct child
        if (isDirectChild(child, parent, compositesChildren)) {
            return true;
        }

        // Case 2: Indirect child
        for (Component<?> childOfParent : compositesChildren.get(parent)) {
            if (childOfParent instanceof Composite) {
                return isRecursiveChild(child, (Composite) childOfParent, compositesChildren);
            }
        }

        // Case 3: Not child of parent
        return false;
    }

    protected static boolean isExcludedFromDelegation(Component<?> provider, Interface providedInterface) {
        String providerName = provider.getValue().getEntityName();
        String providedInterfaceName = providedInterface.getValue().getEntityName();
        return providedInterfaceName.equals(String.format(DELEGATION_EXCLUSION_NAME_PATTERN, providerName));
    }

    private int compareComposites(Composite a, Composite b, Multimap<Composite, Component<?>> compositesChildren) {
        if (isRecursiveChild(a, b, compositesChildren)) {
            return -1;
        } else if (isRecursiveChild(b, a, compositesChildren)) {
            return 1;
        }
        return 0;
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
        String interfaceEntityName = interfaceInstance.getValue()
                .getEntityName();
        return String.format(ROLE_PROVIDES_NAME_PATTERN, interfaceEntityName);
    }

    protected static String getRequiredRoleName(Interface interfaceInstance) {
        String interfaceEntityName = interfaceInstance.getValue()
                .getEntityName();
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
