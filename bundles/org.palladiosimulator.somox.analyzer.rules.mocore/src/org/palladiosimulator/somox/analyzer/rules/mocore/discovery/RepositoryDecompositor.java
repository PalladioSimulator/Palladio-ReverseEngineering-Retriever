package org.palladiosimulator.somox.analyzer.rules.mocore.discovery;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.palladiosimulator.pcm.core.composition.AssemblyConnector;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.composition.Connector;
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
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.AtomicComponent;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Component;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Composite;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Interface;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.ServiceEffectSpecification;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Signature;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.ComponentAssemblyRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.ComponentSignatureProvisionRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.CompositionRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.InterfaceProvisionRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.InterfaceRequirementRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.ServiceEffectSpecificationRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.SignatureProvisionRelation;

import tools.mdsd.mocore.framework.discovery.Discoverer;

/**
 * A decompositor able to extract model-elements and model-relations from a {@link Repository PCM repository}.
 */
public class RepositoryDecompositor implements Decompositor<Repository> {
    @Override
    public Collection<Discoverer<?>> decompose(Repository repository) {
        // Fetch components, interface provisions and requirements, signatures, and service effect specifications
        Set<AtomicComponent> atomicComponents = new HashSet<>();
        Set<Composite> composites = new HashSet<>();
        Set<CompositionRelation> compositions = new HashSet<>();
        Set<InterfaceProvisionRelation> interfaceProvisions = new HashSet<>();
        Set<InterfaceRequirementRelation> interfaceRequirements = new HashSet<>();
        Set<SignatureProvisionRelation> signatureProvisions = new HashSet<>();
        Set<ServiceEffectSpecificationRelation> seffProvisions = new HashSet<>();
        Set<ComponentAssemblyRelation> componentAssemblies = new HashSet<>();

        for (RepositoryComponent repositoryComponent : repository.getComponents__Repository()) {
            Component<?> component;
            if (repositoryComponent instanceof BasicComponent) {
                AtomicComponent atomicComponent = new AtomicComponent((BasicComponent) repositoryComponent, false);
                atomicComponents.add(atomicComponent);
                component = atomicComponent;

                // Basic component specific behavior
                // Fetch service effect specifications from basic component
                for (org.palladiosimulator.pcm.seff.ServiceEffectSpecification seff : atomicComponent.getValue()
                        .getServiceEffectSpecifications__BasicComponent()) {
                    if (seff instanceof ResourceDemandingSEFF) {
                        if (seff.getDescribedService__SEFF() instanceof OperationSignature) {
                            ServiceEffectSpecification seffWrapper = new ServiceEffectSpecification(
                                    (ResourceDemandingSEFF) seff, false);
                            OperationSignature operationSignature = (OperationSignature) seff
                                    .getDescribedService__SEFF();
                            Signature signature = new Signature(operationSignature, false);
                            Interface interFace = new Interface(operationSignature.getInterface__OperationSignature(),
                                    false);
                            SignatureProvisionRelation signatureProvision = new SignatureProvisionRelation(signature,
                                    interFace, false);
                            InterfaceProvisionRelation interfaceProvision = new InterfaceProvisionRelation(component,
                                    interFace, false);
                            ComponentSignatureProvisionRelation componentSignatureProvision = new ComponentSignatureProvisionRelation(
                                    interfaceProvision, signatureProvision, false);
                            seffProvisions.add(new ServiceEffectSpecificationRelation(componentSignatureProvision,
                                    seffWrapper, false));
                        }
                    }
                }
            } else if (repositoryComponent instanceof CompositeComponent) {
                Composite composite = new Composite((CompositeComponent) repositoryComponent, false);
                composites.add(composite);
                component = composite;

                // Composite specific behavior
                // Create composition relations for each composite
                for (AssemblyContext assemblyContext : composite.getValue().getAssemblyContexts__ComposedStructure()) {
                    RepositoryComponent encapsulatedComponent = assemblyContext
                            .getEncapsulatedComponent__AssemblyContext();

                    // Create appropriate wrapper for child component
                    Component<?> childWrapper = getGenericWrapperFor(encapsulatedComponent);
                    if (childWrapper == null) {
                        // Ignore child that cannot be wrapped
                        continue;
                    }

                    // Create composition for composite & child
                    CompositionRelation composition = new CompositionRelation(composite, childWrapper, false);
                    compositions.add(composition);
                }

                for (Connector connector : composite.getValue().getConnectors__ComposedStructure()) {
                    if (connector instanceof AssemblyConnector) {
                        AssemblyConnector assemblyConnector = (AssemblyConnector) connector;

                        // Wrap provider and consumer component
                        Component<?> provider = getGenericWrapperFor(
                                assemblyConnector.getProvidingAssemblyContext_AssemblyConnector()
                                        .getEncapsulatedComponent__AssemblyContext());
                        Component<?> consumer = getGenericWrapperFor(
                                assemblyConnector.getRequiringAssemblyContext_AssemblyConnector()
                                        .getEncapsulatedComponent__AssemblyContext());

                        // Wrap role interfaces
                        Interface providedInterface = new Interface(assemblyConnector
                                .getProvidedRole_AssemblyConnector().getProvidedInterface__OperationProvidedRole(),
                                false);
                        Interface requiredInterface = new Interface(assemblyConnector
                                .getRequiredRole_AssemblyConnector().getRequiredInterface__OperationRequiredRole(),
                                false);

                        // Create interface relations & component assembly relation
                        InterfaceProvisionRelation provisionRelation = new InterfaceProvisionRelation(provider,
                                providedInterface, false);
                        InterfaceRequirementRelation requirementRelation = new InterfaceRequirementRelation(consumer,
                                requiredInterface, false);
                        ComponentAssemblyRelation assemblyRelation = new ComponentAssemblyRelation(provisionRelation,
                                requirementRelation, false);
                        componentAssemblies.add(assemblyRelation);
                    }
                    // TODO Create delegations for composite
                }
            } else {
                // Ignore repository components that are neither basic nor composite
                continue;
            }

            // Behavior for generic repository components
            // Transform provided roles into interface provision relations
            for (ProvidedRole providedRole : repositoryComponent.getProvidedRoles_InterfaceProvidingEntity()) {
                if (providedRole instanceof OperationProvidedRole) {
                    OperationProvidedRole operationProvidedRole = (OperationProvidedRole) providedRole;
                    Interface providerInterface = new Interface(
                            operationProvidedRole.getProvidedInterface__OperationProvidedRole(), false);
                    interfaceProvisions.add(new InterfaceProvisionRelation(component, providerInterface, false));

                    // Create signature provisions for provider interface
                    for (OperationSignature operationSignature : providerInterface.getValue()
                            .getSignatures__OperationInterface()) {
                        Signature signatureWrapper = new Signature(operationSignature, false);
                        signatureProvisions.add(new SignatureProvisionRelation(signatureWrapper,
                                providerInterface, false));
                    }
                }
            }

            // Transform required roles into interface requirement relations
            for (RequiredRole requiredRole : repositoryComponent.getRequiredRoles_InterfaceRequiringEntity()) {
                if (requiredRole instanceof OperationRequiredRole) {
                    OperationRequiredRole operationRequiredRole = (OperationRequiredRole) requiredRole;
                    Interface consumerInterface = new Interface(
                            operationRequiredRole.getRequiredInterface__OperationRequiredRole(), false);
                    interfaceRequirements.add(new InterfaceRequirementRelation(component, consumerInterface, false));

                    // Create signature provisions for consumer interface
                    for (OperationSignature operationSignature : consumerInterface.getValue()
                            .getSignatures__OperationInterface()) {
                        Signature signatureWrapper = new Signature(operationSignature, false);
                        signatureProvisions.add(new SignatureProvisionRelation(signatureWrapper,
                                consumerInterface, false));
                    }
                }
            }
        }

        SimpleDiscoverer<AtomicComponent> atomicComponentDiscoverer = new SimpleDiscoverer<>(atomicComponents,
                AtomicComponent.class);
        SimpleDiscoverer<Composite> compositeDiscoverer = new SimpleDiscoverer<>(composites, Composite.class);
        SimpleDiscoverer<CompositionRelation> compositionDiscoverer = new SimpleDiscoverer<>(compositions,
                CompositionRelation.class);
        SimpleDiscoverer<SignatureProvisionRelation> signatureProvisionDiscoverer = new SimpleDiscoverer<>(
                signatureProvisions, SignatureProvisionRelation.class);
        SimpleDiscoverer<InterfaceProvisionRelation> interfaceProvisionDiscoverer = new SimpleDiscoverer<>(
                interfaceProvisions, InterfaceProvisionRelation.class);
        SimpleDiscoverer<InterfaceRequirementRelation> interfaceRequirementDiscoverer = new SimpleDiscoverer<>(
                interfaceRequirements, InterfaceRequirementRelation.class);
        SimpleDiscoverer<ServiceEffectSpecificationRelation> seffProvisionDiscoverer = new SimpleDiscoverer<>(
                seffProvisions, ServiceEffectSpecificationRelation.class);
        SimpleDiscoverer<ComponentAssemblyRelation> assemblyDiscoverer = new SimpleDiscoverer<>(componentAssemblies,
                ComponentAssemblyRelation.class);
        return List.of(atomicComponentDiscoverer, compositeDiscoverer, compositionDiscoverer,
                signatureProvisionDiscoverer, interfaceProvisionDiscoverer, interfaceRequirementDiscoverer,
                seffProvisionDiscoverer, assemblyDiscoverer);
    }

    private Component<?> getGenericWrapperFor(RepositoryComponent repositoryComponent) {
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
