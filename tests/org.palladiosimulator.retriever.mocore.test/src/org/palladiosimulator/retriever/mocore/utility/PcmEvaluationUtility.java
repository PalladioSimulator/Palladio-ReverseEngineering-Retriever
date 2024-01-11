package org.palladiosimulator.retriever.mocore.utility;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationContext;
import org.palladiosimulator.pcm.core.composition.AssemblyConnector;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.CompositeComponent;
import org.palladiosimulator.pcm.repository.DataType;
import org.palladiosimulator.pcm.repository.Interface;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationRequiredRole;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.repository.ProvidedRole;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryComponent;
import org.palladiosimulator.pcm.repository.RequiredRole;
import org.palladiosimulator.pcm.resourceenvironment.CommunicationLinkResourceSpecification;
import org.palladiosimulator.pcm.resourceenvironment.LinkingResource;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.retriever.mocore.surrogate.element.Component;
import org.palladiosimulator.retriever.mocore.surrogate.element.Deployment;
import org.palladiosimulator.retriever.mocore.surrogate.element.LinkResourceSpecification;
import org.palladiosimulator.retriever.mocore.surrogate.element.ServiceEffectSpecification;
import org.palladiosimulator.retriever.mocore.surrogate.element.Signature;
import org.palladiosimulator.retriever.mocore.surrogate.relation.ComponentAllocationRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.ComponentAssemblyRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.CompositionRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.DeploymentDeploymentRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.InterfaceProvisionRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.InterfaceRequirementRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.ServiceEffectSpecificationRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.SignatureProvisionRelation;

import de.uka.ipd.sdq.identifier.Identifier;

public final class PcmEvaluationUtility {
    private PcmEvaluationUtility() {
        throw new IllegalStateException("Utility class cannot be instantiated.");
    }

    public static boolean representSame(DataType type, DataType otherType) {
        boolean equalType;
        if (type instanceof Identifier && otherType instanceof Identifier) {
            equalType = Objects.equals(((Identifier) type).getId(),
                    ((Identifier) otherType).getId());
        } else {
            equalType = Objects.equals(type, otherType);
        }
        return equalType;
    }

    public static boolean representSame(Parameter parameter, Parameter otherParameter) {
        boolean equalName = Objects.equals(parameter.getParameterName(), otherParameter.getParameterName());
        boolean equalType = representSame(parameter.getDataType__Parameter(), otherParameter.getDataType__Parameter());
        return equalName && equalType;
    }

    public static boolean representSame(OperationSignature signature,
            org.palladiosimulator.pcm.repository.Signature otherSignature) {
        if (otherSignature instanceof OperationSignature) {
            return representSame(signature, (OperationSignature) otherSignature);
        }
        return false;
    }

    public static boolean representSame(OperationSignature signature, OperationSignature otherSignature) {
        boolean equalName = Objects.equals(signature.getEntityName(), otherSignature.getEntityName());
        boolean equalReturn = representSame(signature.getReturnType__OperationSignature(),
                otherSignature.getReturnType__OperationSignature());
        boolean equalParameters = areCollectionsEqual(signature.getParameters__OperationSignature(),
                otherSignature.getParameters__OperationSignature(), PcmEvaluationUtility::representSame);
        return equalName && equalReturn && equalParameters;
    }

    public static boolean representSame(OperationInterface interFace, Interface otherInterFace) {
        if (otherInterFace instanceof OperationInterface) {
            return representSame(interFace, (OperationInterface) otherInterFace);
        }
        return false;
    }

    public static boolean representSame(OperationInterface interFace, OperationInterface otherInterFace) {
        boolean equalName = Objects.equals(interFace.getEntityName(), otherInterFace.getEntityName());
        // TODO Check characterization & protocol => Is there a palladio equal check?
        return equalName;
    }

    public static boolean representSame(RepositoryComponent component, RepositoryComponent otherComponent) {
        if (otherComponent instanceof BasicComponent && component instanceof BasicComponent) {
            return representSame((BasicComponent) component, (BasicComponent) otherComponent);
        } else if (otherComponent instanceof CompositeComponent && component instanceof CompositeComponent) {
            return representSame((CompositeComponent) component, (CompositeComponent) otherComponent);
        }
        return false;
    }

    public static boolean representSame(BasicComponent component, BasicComponent otherComponent) {
        boolean equalName = Objects.equals(component.getEntityName(), otherComponent.getEntityName());
        boolean equalType = Objects.equals(component.getComponentType(), otherComponent.getComponentType());
        // TODO Check parameter usage => Is there a palladio equal check?
        return equalName && equalType;
    }

    public static boolean representSame(CompositeComponent component, CompositeComponent otherComponent) {
        boolean equalName = Objects.equals(component.getEntityName(), otherComponent.getEntityName());
        boolean equalType = Objects.equals(component.getComponentType(), otherComponent.getComponentType());
        // TODO Check parameter usage => Is there a palladio equal check?
        return equalName && equalType;
    }

    public static boolean representSame(ResourceContainer container, ResourceContainer otherContainer) {
        boolean equalName = Objects.equals(container.getEntityName(), otherContainer.getEntityName());
        // TODO ResourceSpecifications are removed from old container on copy. Consequently, comparing it is not
        // possible.
        return equalName;
    }

    public static boolean representSame(ResourceDemandingSEFF seff,
            org.palladiosimulator.pcm.seff.ServiceEffectSpecification otherSeff) {
        if (otherSeff instanceof ResourceDemandingSEFF) {
            return representSame(seff, (ResourceDemandingSEFF) otherSeff);
        }
        return false;
    }

    public static boolean representSame(ResourceDemandingSEFF seff, ResourceDemandingSEFF otherSeff) {
        boolean equalIdentifier = Objects.equals(seff.getId(), otherSeff.getId());
        boolean equalTypeIdentifier = Objects.equals(seff.getSeffTypeID(), otherSeff.getSeffTypeID());
        boolean equalSteps = areCollectionsEqualIgnoringOrder(
                mapToIdentifier(seff.getSteps_Behaviour()),
                mapToIdentifier(otherSeff.getSteps_Behaviour()));
        boolean equalInternalBehaviors = areCollectionsEqualIgnoringOrder(
                mapToIdentifier(seff.getResourceDemandingInternalBehaviours()),
                mapToIdentifier(otherSeff.getResourceDemandingInternalBehaviours()));
        boolean equalLoopAction = Objects.equals(
                mapToIdentifier(seff.getAbstractLoopAction_ResourceDemandingBehaviour()),
                mapToIdentifier(otherSeff.getAbstractLoopAction_ResourceDemandingBehaviour()));
        boolean equalBranchTransition = Objects.equals(
                mapToIdentifier(seff.getAbstractBranchTransition_ResourceDemandingBehaviour()),
                mapToIdentifier(otherSeff.getAbstractBranchTransition_ResourceDemandingBehaviour()));
        return equalIdentifier && equalTypeIdentifier && equalSteps && equalInternalBehaviors
                && equalLoopAction && equalBranchTransition;
    }

    public static Optional<ResourceContainer> getRepresentative(ResourceEnvironment resourceEnvironment,
            Deployment container) {
        List<ResourceContainer> containers = resourceEnvironment.getResourceContainer_ResourceEnvironment();
        for (ResourceContainer environmentContainer : containers) {
            if (representSame(container.getValue(), environmentContainer)) {
                return Optional.of(environmentContainer);
            }
        }
        return Optional.empty();
    }

    public static Optional<RepositoryComponent> getRepresentative(Repository repository, Component<?> component) {
        List<RepositoryComponent> components = repository.getComponents__Repository();
        for (RepositoryComponent repositoryComponent : components) {
            if (representSame(component.getValue(), repositoryComponent)) {
                return Optional.of(repositoryComponent);
            }
        }
        return Optional.empty();
    }

    public static Optional<OperationInterface> getRepresentative(Repository repository,
            org.palladiosimulator.retriever.mocore.surrogate.element.Interface interFace) {
        List<Interface> interfaces = repository.getInterfaces__Repository();
        for (Interface repositoryInterface : interfaces) {
            if (representSame(interFace.getValue(), repositoryInterface)) {
                return Optional.of((OperationInterface) repositoryInterface);
            }
        }
        return Optional.empty();
    }

    public static boolean containsRepresentative(Repository repository, Component<?> component) {
        return getRepresentative(repository, component).isPresent();
    }

    public static boolean containsRepresentative(Repository repository, CompositionRelation composition) {
        CompositeComponent wrappedComposite = composition.getSource().getValue();
        RepositoryComponent wrappedChild = composition.getDestination().getValue();
        return repository.getComponents__Repository()
                .stream()
                .filter(component -> component instanceof CompositeComponent)
                .map(component -> (CompositeComponent) component)
                .filter(composite -> composite.getEntityName().equals(wrappedComposite.getEntityName()))
                .flatMap(composite -> composite.getAssemblyContexts__ComposedStructure().stream())
                .anyMatch(assemblyContext -> assemblyContext.getEncapsulatedComponent__AssemblyContext().getEntityName()
                        .equals(wrappedChild.getEntityName()));
    }

    public static boolean containsRepresentative(Repository repository,
            org.palladiosimulator.retriever.mocore.surrogate.element.Interface interFace) {
        return getRepresentative(repository, interFace).isPresent();
    }

    public static boolean containsRepresentative(Repository repository,
            InterfaceProvisionRelation interfaceProvision) {
        OperationInterface wrappedInterface = interfaceProvision.getDestination().getValue();
        Optional<RepositoryComponent> optionalComponent = getRepresentative(repository, interfaceProvision.getSource());
        if (optionalComponent.isPresent()) {
            List<ProvidedRole> roles = optionalComponent.get().getProvidedRoles_InterfaceProvidingEntity();
            return roles.stream()
                    .filter(role -> role instanceof OperationProvidedRole)
                    .map(role -> (OperationProvidedRole) role)
                    .map(OperationProvidedRole::getProvidedInterface__OperationProvidedRole)
                    .anyMatch(interFace -> representSame(wrappedInterface, interFace));
        } else {
            return false;
        }
    }

    public static boolean containsRepresentative(Repository repository,
            InterfaceRequirementRelation interfaceRequirement) {
        OperationInterface wrappedInterface = interfaceRequirement.getDestination().getValue();
        Optional<RepositoryComponent> optionalComponent = getRepresentative(repository,
                interfaceRequirement.getSource());
        if (optionalComponent.isPresent()) {
            List<RequiredRole> roles = optionalComponent.get().getRequiredRoles_InterfaceRequiringEntity();
            return roles.stream()
                    .filter(role -> role instanceof OperationRequiredRole)
                    .map(role -> (OperationRequiredRole) role)
                    .map(OperationRequiredRole::getRequiredInterface__OperationRequiredRole)
                    .anyMatch(interFace -> representSame(wrappedInterface, interFace));
        } else {
            return false;
        }
    }

    public static boolean containsRepresentative(Repository repository, SignatureProvisionRelation signatureProvision) {
        Optional<OperationInterface> optionalOperationInterface = getRepresentative(repository,
                signatureProvision.getDestination());
        return optionalOperationInterface.isPresent()
                && optionalOperationInterface.get().getSignatures__OperationInterface().stream()
                        .anyMatch(signature -> representSame(signatureProvision.getSource().getValue(), signature));
    }

    public static boolean containsRepresentative(Repository repository,
            ServiceEffectSpecificationRelation seffProvision) {
        Component<?> provider = seffProvision.getSource().getSource().getSource();
        Signature signature = seffProvision.getSource().getDestination().getSource();
        ServiceEffectSpecification seff = seffProvision.getDestination();

        Optional<RepositoryComponent> optionalComponent = getRepresentative(repository, provider);
        if (optionalComponent.isPresent() && optionalComponent.get() instanceof BasicComponent) {
            BasicComponent component = (BasicComponent) optionalComponent.get();
            for (org.palladiosimulator.pcm.seff.ServiceEffectSpecification componentSeff : component
                    .getServiceEffectSpecifications__BasicComponent()) {
                if (representSame(seff.getValue(), componentSeff)) {
                    ResourceDemandingSEFF componentRdSeff = (ResourceDemandingSEFF) componentSeff;
                    return representSame(provider.getValue(),
                            componentRdSeff.getBasicComponent_ServiceEffectSpecification())
                            && representSame(signature.getValue(), componentRdSeff.getDescribedService__SEFF())
                            && containsRepresentative(repository, seffProvision.getSource().getSource())
                            && containsRepresentative(repository, seffProvision.getSource().getDestination());
                }
            }
        }
        return false;
    }

    public static boolean containsRepresentative(ResourceEnvironment resourceEnvironment, Deployment container) {
        return getRepresentative(resourceEnvironment, container).isPresent();
    }

    public static boolean containsRepresentative(ResourceEnvironment resourceEnvironment,
            DeploymentDeploymentRelation link) {
        List<LinkingResource> linkingResources = resourceEnvironment.getLinkingResources__ResourceEnvironment();
        for (LinkingResource linkingResource : linkingResources) {
            List<ResourceContainer> linkedContainers = new LinkedList<>(
                    linkingResource.getConnectedResourceContainers_LinkingResource());
            boolean containsContainers = true;
            for (Deployment deployment : List.of(link.getSource(), link.getDestination())) {
                containsContainers = containsContainers
                        && linkedContainers.removeIf(element -> representSame(deployment.getValue(), element));
            }
            if (containsContainers) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsRepresentative(ResourceEnvironment resourceEnvironment,
            LinkResourceSpecification relationSpecification, Collection<Deployment> deployments) {
        CommunicationLinkResourceSpecification specification = relationSpecification.getValue();
        List<LinkingResource> linkingResources = resourceEnvironment.getLinkingResources__ResourceEnvironment();
        for (LinkingResource linkingResource : linkingResources) {
            List<ResourceContainer> linkedContainers = new LinkedList<>(
                    linkingResource.getConnectedResourceContainers_LinkingResource());
            CommunicationLinkResourceSpecification linkSpecification = linkingResource
                    .getCommunicationLinkResourceSpecifications_LinkingResource();
            boolean containsContainers = true;
            for (Deployment deployment : deployments) {
                containsContainers = containsContainers
                        && linkedContainers.removeIf(element -> representSame(deployment.getValue(), element));
            }
            if (containsContainers && linkedContainers.isEmpty()) {
                if (specification.getId().equals(linkSpecification.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean containsRepresentative(Allocation allocation,
            ComponentAllocationRelation allocationRelation) {
        Component<?> component = allocationRelation.getSource();
        Deployment deployment = allocationRelation.getDestination();

        List<AllocationContext> allocationContexts = allocation.getAllocationContexts_Allocation();
        for (AllocationContext allocationContext : allocationContexts) {
            if (representSame(deployment.getValue(), allocationContext.getResourceContainer_AllocationContext())) {
                AssemblyContext assemblyContext = allocationContext.getAssemblyContext_AllocationContext();
                if (representSame(component.getValue(), assemblyContext.getEncapsulatedComponent__AssemblyContext())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean containsRepresentative(System system, Component<?> component) {
        List<AssemblyContext> assemblyContexts = system.getAssemblyContexts__ComposedStructure();
        for (AssemblyContext assemblyContext : assemblyContexts) {
            if (representSame(component.getValue(), assemblyContext.getEncapsulatedComponent__AssemblyContext())) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsRepresentative(System system, ComponentAssemblyRelation assemblyRelation) {
        RepositoryComponent provider = assemblyRelation.getSource().getSource().getValue();
        RepositoryComponent consumer = assemblyRelation.getDestination().getSource().getValue();
        OperationInterface providerConsumerInterface = assemblyRelation.getSource().getDestination().getValue();

        List<AssemblyConnector> assemblyConnectors = system.getConnectors__ComposedStructure().stream()
                .filter(connector -> connector instanceof AssemblyConnector)
                .map(connector -> (AssemblyConnector) connector)
                .collect(Collectors.toList());
        for (AssemblyConnector connector : assemblyConnectors) {
            RepositoryComponent connectorProvider = connector.getProvidingAssemblyContext_AssemblyConnector()
                    .getEncapsulatedComponent__AssemblyContext();
            RepositoryComponent connectorConsumer = connector.getRequiringAssemblyContext_AssemblyConnector()
                    .getEncapsulatedComponent__AssemblyContext();
            OperationInterface connectorProviderConsumerInterface = connector.getProvidedRole_AssemblyConnector()
                    .getProvidedInterface__OperationProvidedRole();

            boolean sameProvider = representSame(provider, connectorProvider);
            boolean sameConsumer = representSame(consumer, connectorConsumer);
            boolean sameInterface = representSame(providerConsumerInterface, connectorProviderConsumerInterface);
            if (sameProvider && sameConsumer && sameInterface) {
                return true;
            }
        }
        return false;
    }

    private static <T> boolean areCollectionsEqual(Collection<T> collection,
            Collection<T> otherCollection, BiFunction<T, T, Boolean> comparisonFunction) {
        if (collection.isEmpty() && otherCollection.isEmpty()) {
            return true;
        } else if (collection.size() != otherCollection.size()) {
            return false;
        }

        List<T> list = new LinkedList<>(collection);
        List<T> otherList = new LinkedList<>(otherCollection);
        for (int i = 0; i < list.size(); i++) {
            if (!comparisonFunction.apply(list.get(i), otherList.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static <T> boolean areCollectionsEqualIgnoringOrder(Collection<T> collection,
            Collection<T> otherCollection) {
        return collection.containsAll(otherCollection) && otherCollection.containsAll(collection);
    }

    private static <T extends Identifier> String mapToIdentifier(T element) {
        return element != null ? element.getId() : null;
    }

    private static List<String> mapToIdentifier(Collection<? extends Identifier> collection) {
        return collection.stream()
                .dropWhile(element -> element == null)
                .map(Identifier::getId)
                .collect(Collectors.toList());
    }
}
