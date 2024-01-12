package org.palladiosimulator.retriever.mocore.discovery;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.palladiosimulator.pcm.core.composition.AssemblyConnector;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.composition.Connector;
import org.palladiosimulator.pcm.core.composition.ProvidedDelegationConnector;
import org.palladiosimulator.pcm.core.composition.RequiredDelegationConnector;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.CompositeComponent;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationRequiredRole;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.ProvidedRole;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryComponent;
import org.palladiosimulator.pcm.repository.RequiredRole;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.retriever.mocore.surrogate.element.AtomicComponent;
import org.palladiosimulator.retriever.mocore.surrogate.element.Component;
import org.palladiosimulator.retriever.mocore.surrogate.element.Composite;
import org.palladiosimulator.retriever.mocore.surrogate.element.Interface;
import org.palladiosimulator.retriever.mocore.surrogate.element.ServiceEffectSpecification;
import org.palladiosimulator.retriever.mocore.surrogate.element.Signature;
import org.palladiosimulator.retriever.mocore.surrogate.relation.ComponentAssemblyRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.ComponentSignatureProvisionRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.CompositeProvisionDelegationRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.CompositeRequirementDelegationRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.CompositionRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.InterfaceProvisionRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.InterfaceRequirementRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.ServiceEffectSpecificationRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.SignatureProvisionRelation;

import tools.mdsd.mocore.framework.discovery.Discoverer;

/**
 * A decompositor able to extract model-elements and model-relations from a {@link Repository PCM
 * repository}.
 */
public class RepositoryDecompositor implements Decompositor<Repository> {
    @Override
    public Collection<Discoverer<?>> decompose(final Repository repository) {
        // Fetch components, interface provisions and requirements, signatures, and service effect
        // specifications
        final Set<AtomicComponent> atomicComponents = new HashSet<>();
        final Set<Composite> composites = new HashSet<>();
        final Set<CompositionRelation> compositions = new HashSet<>();
        final Set<InterfaceProvisionRelation> interfaceProvisions = new HashSet<>();
        final Set<InterfaceRequirementRelation> interfaceRequirements = new HashSet<>();
        final Set<SignatureProvisionRelation> signatureProvisions = new HashSet<>();
        final Set<ServiceEffectSpecificationRelation> seffProvisions = new HashSet<>();
        final Set<ComponentAssemblyRelation> componentAssemblies = new HashSet<>();
        final Set<CompositeProvisionDelegationRelation> provisionDelegations = new HashSet<>();
        final Set<CompositeRequirementDelegationRelation> requirementDelegations = new HashSet<>();

        for (final RepositoryComponent repositoryComponent : repository.getComponents__Repository()) {
            Component<?> component;
            if (repositoryComponent instanceof BasicComponent) {
                final AtomicComponent atomicComponent = new AtomicComponent((BasicComponent) repositoryComponent,
                        false);
                atomicComponents.add(atomicComponent);
                component = atomicComponent;

                // Basic component specific behavior
                // Fetch service effect specifications from basic component
                for (final org.palladiosimulator.pcm.seff.ServiceEffectSpecification seff : atomicComponent.getValue()
                    .getServiceEffectSpecifications__BasicComponent()) {
                    if (seff instanceof ResourceDemandingSEFF) {
                        if (seff.getDescribedService__SEFF() instanceof OperationSignature) {
                            final ServiceEffectSpecification seffWrapper = new ServiceEffectSpecification(
                                    (ResourceDemandingSEFF) seff, false);
                            final OperationSignature operationSignature = (OperationSignature) seff
                                .getDescribedService__SEFF();
                            final Signature signature = new Signature(operationSignature, false);
                            final Interface interFace = new Interface(
                                    operationSignature.getInterface__OperationSignature(), false);
                            final SignatureProvisionRelation signatureProvision = new SignatureProvisionRelation(
                                    signature, interFace, false);
                            final InterfaceProvisionRelation interfaceProvision = new InterfaceProvisionRelation(
                                    component, interFace, false);
                            final ComponentSignatureProvisionRelation componentSignatureProvision = new ComponentSignatureProvisionRelation(
                                    interfaceProvision, signatureProvision, false);
                            seffProvisions.add(new ServiceEffectSpecificationRelation(componentSignatureProvision,
                                    seffWrapper, false));
                        }
                    }
                }
            } else if (repositoryComponent instanceof CompositeComponent) {
                final Composite composite = new Composite((CompositeComponent) repositoryComponent, false);
                composites.add(composite);
                component = composite;

                // Composite specific behavior
                // Create composition relations for each composite
                for (final AssemblyContext assemblyContext : composite.getValue()
                    .getAssemblyContexts__ComposedStructure()) {
                    final RepositoryComponent encapsulatedComponent = assemblyContext
                        .getEncapsulatedComponent__AssemblyContext();

                    // Create appropriate wrapper for child component
                    final Component<?> childWrapper = this.getGenericWrapperFor(encapsulatedComponent);
                    if (childWrapper == null) {
                        // Ignore child that cannot be wrapped
                        continue;
                    }

                    // Create composition for composite & child
                    final CompositionRelation composition = new CompositionRelation(composite, childWrapper, false);
                    compositions.add(composition);
                }

                // Process connectors of composite component
                for (final Connector connector : composite.getValue()
                    .getConnectors__ComposedStructure()) {
                    if (connector instanceof AssemblyConnector) {
                        final AssemblyConnector assemblyConnector = (AssemblyConnector) connector;

                        // Wrap provider and consumer component
                        final Component<?> provider = this
                            .getGenericWrapperFor(assemblyConnector.getProvidingAssemblyContext_AssemblyConnector()
                                .getEncapsulatedComponent__AssemblyContext());
                        final Component<?> consumer = this
                            .getGenericWrapperFor(assemblyConnector.getRequiringAssemblyContext_AssemblyConnector()
                                .getEncapsulatedComponent__AssemblyContext());

                        // Wrap role interfaces
                        final Interface providedInterface = new Interface(
                                assemblyConnector.getProvidedRole_AssemblyConnector()
                                    .getProvidedInterface__OperationProvidedRole(),
                                false);
                        final Interface requiredInterface = new Interface(
                                assemblyConnector.getRequiredRole_AssemblyConnector()
                                    .getRequiredInterface__OperationRequiredRole(),
                                false);

                        // Create interface relations & component assembly relation
                        final InterfaceProvisionRelation provisionRelation = new InterfaceProvisionRelation(provider,
                                providedInterface, false);
                        final InterfaceRequirementRelation requirementRelation = new InterfaceRequirementRelation(
                                consumer, requiredInterface, false);
                        final ComponentAssemblyRelation assemblyRelation = new ComponentAssemblyRelation(
                                provisionRelation, requirementRelation, false);

                        // Add assembly to discoverer
                        componentAssemblies.add(assemblyRelation);
                    } else if (connector instanceof ProvidedDelegationConnector) {
                        final ProvidedDelegationConnector providedDelegationConnector = (ProvidedDelegationConnector) connector;

                        // Wrap the providing component & the inner and outer role's interfaces
                        final Component<?> connectorComponent = this.getGenericWrapperFor(
                                providedDelegationConnector.getAssemblyContext_ProvidedDelegationConnector()
                                    .getEncapsulatedComponent__AssemblyContext());
                        final Interface innerInterface = new Interface(
                                providedDelegationConnector.getInnerProvidedRole_ProvidedDelegationConnector()
                                    .getProvidedInterface__OperationProvidedRole(),
                                false);
                        final Interface outerInterface = new Interface(
                                providedDelegationConnector.getOuterProvidedRole_ProvidedDelegationConnector()
                                    .getProvidedInterface__OperationProvidedRole(),
                                false);

                        // Create interface relations
                        final InterfaceProvisionRelation innerInterfaceRelation = new InterfaceProvisionRelation(
                                connectorComponent, innerInterface, false);
                        final InterfaceProvisionRelation outerInterfaceRelation = new InterfaceProvisionRelation(
                                composite, outerInterface, false);
                        final CompositeProvisionDelegationRelation delegationRelation = new CompositeProvisionDelegationRelation(
                                outerInterfaceRelation, innerInterfaceRelation, false);

                        // Add delegation relation to discoverer
                        provisionDelegations.add(delegationRelation);
                    } else if (connector instanceof RequiredDelegationConnector) {
                        final RequiredDelegationConnector requiredDelegationConnector = (RequiredDelegationConnector) connector;

                        // Wrap the requiring component & the inner and outer role's interfaces
                        final Component<?> connectorComponent = this.getGenericWrapperFor(
                                requiredDelegationConnector.getAssemblyContext_RequiredDelegationConnector()
                                    .getEncapsulatedComponent__AssemblyContext());
                        final Interface innerInterface = new Interface(
                                requiredDelegationConnector.getInnerRequiredRole_RequiredDelegationConnector()
                                    .getRequiredInterface__OperationRequiredRole(),
                                false);
                        final Interface outerInterface = new Interface(
                                requiredDelegationConnector.getOuterRequiredRole_RequiredDelegationConnector()
                                    .getRequiredInterface__OperationRequiredRole(),
                                false);

                        // Create interface relations & delegation relation
                        final InterfaceRequirementRelation innerInterfaceRelation = new InterfaceRequirementRelation(
                                connectorComponent, innerInterface, false);
                        final InterfaceRequirementRelation outerInterfaceRelation = new InterfaceRequirementRelation(
                                composite, outerInterface, false);
                        final CompositeRequirementDelegationRelation delegationRelation = new CompositeRequirementDelegationRelation(
                                outerInterfaceRelation, innerInterfaceRelation, false);

                        // Add delegation relation to discoverer
                        requirementDelegations.add(delegationRelation);
                    }
                }
            } else {
                // Ignore repository components that are neither basic nor composite
                continue;
            }

            // Behavior for generic repository components
            // Transform provided roles into interface provision relations
            for (final ProvidedRole providedRole : repositoryComponent.getProvidedRoles_InterfaceProvidingEntity()) {
                if (providedRole instanceof OperationProvidedRole) {
                    final OperationProvidedRole operationProvidedRole = (OperationProvidedRole) providedRole;
                    final Interface providerInterface = new Interface(
                            operationProvidedRole.getProvidedInterface__OperationProvidedRole(), false);
                    interfaceProvisions.add(new InterfaceProvisionRelation(component, providerInterface, false));

                    // Create signature provisions for provider interface
                    for (final OperationSignature operationSignature : providerInterface.getValue()
                        .getSignatures__OperationInterface()) {
                        final Signature signatureWrapper = new Signature(operationSignature, false);
                        signatureProvisions
                            .add(new SignatureProvisionRelation(signatureWrapper, providerInterface, false));
                    }
                }
            }

            // Transform required roles into interface requirement relations
            for (final RequiredRole requiredRole : repositoryComponent.getRequiredRoles_InterfaceRequiringEntity()) {
                if (requiredRole instanceof OperationRequiredRole) {
                    final OperationRequiredRole operationRequiredRole = (OperationRequiredRole) requiredRole;
                    final Interface consumerInterface = new Interface(
                            operationRequiredRole.getRequiredInterface__OperationRequiredRole(), false);
                    interfaceRequirements.add(new InterfaceRequirementRelation(component, consumerInterface, false));

                    // Create signature provisions for consumer interface
                    for (final OperationSignature operationSignature : consumerInterface.getValue()
                        .getSignatures__OperationInterface()) {
                        final Signature signatureWrapper = new Signature(operationSignature, false);
                        signatureProvisions
                            .add(new SignatureProvisionRelation(signatureWrapper, consumerInterface, false));
                    }
                }
            }
        }

        final SimpleDiscoverer<AtomicComponent> atomicComponentDiscoverer = new SimpleDiscoverer<>(atomicComponents,
                AtomicComponent.class);
        final SimpleDiscoverer<Composite> compositeDiscoverer = new SimpleDiscoverer<>(composites, Composite.class);
        final SimpleDiscoverer<CompositionRelation> compositionDiscoverer = new SimpleDiscoverer<>(compositions,
                CompositionRelation.class);
        final SimpleDiscoverer<SignatureProvisionRelation> signatureProvisionDiscoverer = new SimpleDiscoverer<>(
                signatureProvisions, SignatureProvisionRelation.class);
        final SimpleDiscoverer<InterfaceProvisionRelation> interfaceProvisionDiscoverer = new SimpleDiscoverer<>(
                interfaceProvisions, InterfaceProvisionRelation.class);
        final SimpleDiscoverer<InterfaceRequirementRelation> interfaceRequirementDiscoverer = new SimpleDiscoverer<>(
                interfaceRequirements, InterfaceRequirementRelation.class);
        final SimpleDiscoverer<ServiceEffectSpecificationRelation> seffProvisionDiscoverer = new SimpleDiscoverer<>(
                seffProvisions, ServiceEffectSpecificationRelation.class);
        final SimpleDiscoverer<ComponentAssemblyRelation> assemblyDiscoverer = new SimpleDiscoverer<>(
                componentAssemblies, ComponentAssemblyRelation.class);
        final SimpleDiscoverer<CompositeProvisionDelegationRelation> provisionDelegationDiscoverer = new SimpleDiscoverer<>(
                provisionDelegations, CompositeProvisionDelegationRelation.class);
        final SimpleDiscoverer<CompositeRequirementDelegationRelation> requirementDelegationDiscoverer = new SimpleDiscoverer<>(
                requirementDelegations, CompositeRequirementDelegationRelation.class);
        return List.of(atomicComponentDiscoverer, compositeDiscoverer, compositionDiscoverer,
                signatureProvisionDiscoverer, interfaceProvisionDiscoverer, interfaceRequirementDiscoverer,
                seffProvisionDiscoverer, assemblyDiscoverer, provisionDelegationDiscoverer,
                requirementDelegationDiscoverer);
    }

    private Component<?> getGenericWrapperFor(final RepositoryComponent repositoryComponent) {
        // Ignore components that are neither basic nor composite
        Component<?> wrapper = null;
        if (repositoryComponent instanceof BasicComponent) {
            wrapper = new AtomicComponent((BasicComponent) repositoryComponent, false);
        } else if (repositoryComponent instanceof CompositeComponent) {
            wrapper = new Composite((CompositeComponent) repositoryComponent, false);
        }
        return wrapper;
    }
}
