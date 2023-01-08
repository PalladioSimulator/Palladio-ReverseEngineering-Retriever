package org.palladiosimulator.somox.analyzer.rules.mocore.discovery;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationRequiredRole;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.ProvidedRole;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryComponent;
import org.palladiosimulator.pcm.repository.RequiredRole;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Component;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Interface;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.ServiceEffectSpecification;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Signature;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.ComponentSignatureProvisionRelation;
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
        Set<Component> components = new HashSet<>();
        Set<InterfaceProvisionRelation> interfaceProvisions = new HashSet<>();
        Set<InterfaceRequirementRelation> interfaceRequirements = new HashSet<>();
        Set<SignatureProvisionRelation> signatureProvisions = new HashSet<>();
        Set<ServiceEffectSpecificationRelation> seffProvisions = new HashSet<>();
        for (RepositoryComponent repositoryComponent : repository.getComponents__Repository()) {
            Component component;
            if (repositoryComponent instanceof BasicComponent) {
                component = new Component((BasicComponent) repositoryComponent, false);
                components.add(component);
            } else {
                continue;
            }

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

            // Fetch service effect specifications from component
            for (org.palladiosimulator.pcm.seff.ServiceEffectSpecification seff : component.getValue()
                    .getServiceEffectSpecifications__BasicComponent()) {
                if (seff instanceof ResourceDemandingSEFF) {
                    if (seff.getDescribedService__SEFF() instanceof OperationSignature) {
                        ServiceEffectSpecification seffWrapper = new ServiceEffectSpecification(
                                (ResourceDemandingSEFF) seff, false);
                        OperationSignature operationSignature = (OperationSignature) seff.getDescribedService__SEFF();
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
        }

        SimpleDiscoverer<Component> componentDiscoverer = new SimpleDiscoverer<>(components, Component.class);
        SimpleDiscoverer<SignatureProvisionRelation> signatureProvisionDiscoverer = new SimpleDiscoverer<>(
                signatureProvisions, SignatureProvisionRelation.class);
        SimpleDiscoverer<InterfaceProvisionRelation> interfaceProvisionDiscoverer = new SimpleDiscoverer<>(
                interfaceProvisions, InterfaceProvisionRelation.class);
        SimpleDiscoverer<InterfaceRequirementRelation> interfaceRequirementDiscoverer = new SimpleDiscoverer<>(
                interfaceRequirements, InterfaceRequirementRelation.class);
        SimpleDiscoverer<ServiceEffectSpecificationRelation> seffProvisionDiscoverer = new SimpleDiscoverer<>(
                seffProvisions, ServiceEffectSpecificationRelation.class);
        return List.of(componentDiscoverer, signatureProvisionDiscoverer,
                interfaceProvisionDiscoverer, interfaceRequirementDiscoverer, seffProvisionDiscoverer);
    }
}
