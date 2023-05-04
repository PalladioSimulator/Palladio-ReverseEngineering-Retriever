package org.palladiosimulator.somox.analyzer.rules.workflow;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.palladiosimulator.pcm.repository.BasicComponent;
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

import de.uka.ipd.sdq.workflow.blackboard.Blackboard;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class SeffMergerJob implements IBlackboardInteractingJob<Blackboard<Object>> {
    private static final String JOB_NAME = "ServiceEffectSpecification Repository Merger Job";

    private Blackboard<Object> blackboard;

    private final String sourceSeffRepositoryKey;
    private final String destinationSeffRepositoryKey;

    public SeffMergerJob(Blackboard<Object> blackboard, String sourceSeffRepositoryKey,
            String destinationSeffRepositoryKey) {
        this.blackboard = Objects.requireNonNull(blackboard);
        this.sourceSeffRepositoryKey = sourceSeffRepositoryKey;
        this.destinationSeffRepositoryKey = destinationSeffRepositoryKey;
    }

    @Override
    public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
        // Fetch input from blackboard
        monitor.subTask("Retrieving source and destination repository from blackboard");
        Repository sourceRepository = (Repository) this.blackboard.getPartition(this.sourceSeffRepositoryKey);
        Repository destinationRepository = (Repository) this.blackboard.getPartition(this.destinationSeffRepositoryKey);

        // Move seffs from source to destination repository
        monitor.subTask("Merging ServiceEffectSpecificications from source with destination repository");
        for (RepositoryComponent component : sourceRepository.getComponents__Repository()) {
            if (!(component instanceof BasicComponent)) {
                continue;
            }

            // Assumes that each component from source repository has a counterpart with the same name in destination
            // repository. Otherwise, exception is thrown.
            BasicComponent sourceComponent = (BasicComponent) component;
            BasicComponent destinationComponent = (BasicComponent) destinationRepository.getComponents__Repository()
                    .stream()
                    .filter(otherComponent -> otherComponent.getEntityName().equals(sourceComponent.getEntityName()))
                    .findFirst().orElseThrow();

            // Overwrite seffs within destination component
            for (ServiceEffectSpecification sourceSeff : sourceComponent
                    .getServiceEffectSpecifications__BasicComponent()) {
                // Retrieve destination signature for seff, throw if signature is not provided by destination component
                OperationSignature destinationSignature = destinationComponent
                        .getProvidedRoles_InterfaceProvidingEntity()
                        .stream()
                        .filter(role -> role instanceof OperationProvidedRole)
                        .map(role -> (OperationProvidedRole) role)
                        .flatMap(role -> role.getProvidedInterface__OperationProvidedRole()
                                .getSignatures__OperationInterface()
                                .stream())
                        .filter(signature -> signature.getEntityName()
                                .equals(sourceSeff.getDescribedService__SEFF().getEntityName()))
                        .findFirst().orElseThrow();

                // Set component and signature of source seff to destination elements
                sourceSeff.setBasicComponent_ServiceEffectSpecification(destinationComponent);
                sourceSeff.setDescribedService__SEFF(destinationSignature);

                // Adapt external call actions to new repository -> Swap signatures and required roles
                EList<AbstractAction> behaviorSteps = ((ResourceDemandingSEFF) sourceSeff).getSteps_Behaviour();
                for (AbstractAction action : behaviorSteps) {
                    if (!(action instanceof ExternalCallAction)) {
                        continue;
                    }
                    ExternalCallAction externalCallAction = (ExternalCallAction) action;
                    String calledSignatureEntityName = externalCallAction.getCalledService_ExternalService()
                            .getEntityName();

                    // Fetch called signature from destination repository
                    OperationSignature calledSignature = destinationRepository.getInterfaces__Repository().stream()
                            .filter(interFace -> interFace instanceof OperationInterface)
                            .flatMap(interFace -> ((OperationInterface) interFace).getSignatures__OperationInterface()
                                    .stream())
                            .filter(signature -> signature.getEntityName().equals(calledSignatureEntityName))
                            .findFirst().orElseThrow();

                    // Fetch required role from destination repository
                    OperationRequiredRole requiredRole = destinationComponent
                            .getRequiredRoles_InterfaceRequiringEntity().stream()
                            .filter(role -> role instanceof OperationRequiredRole)
                            .map(role -> (OperationRequiredRole) role)
                            .filter(role -> role.getRequiredInterface__OperationRequiredRole()
                                    .getSignatures__OperationInterface().contains(calledSignature))
                            .findFirst().orElseThrow();

                    externalCallAction.setCalledService_ExternalService(calledSignature);
                    externalCallAction.setRole_ExternalService(requiredRole);
                }

                // Find optional already existing and conflicting seff in destination component
                Optional<ServiceEffectSpecification> optionalDestinationSeff = destinationComponent
                        .getServiceEffectSpecifications__BasicComponent().stream()
                        .filter(destinationSeff -> destinationSeff.getDescribedService__SEFF().getEntityName()
                                .equals(sourceSeff.getDescribedService__SEFF().getEntityName()))
                        .findFirst();

                // Delete conflicting seff in destination component
                if (optionalDestinationSeff.isPresent()) {
                    destinationComponent.getServiceEffectSpecifications__BasicComponent()
                            .remove(optionalDestinationSeff.get());
                }
            }
        }
        monitor.done();
    }

    @Override
    public void cleanup(IProgressMonitor monitor) throws CleanupFailedException {
        // No cleanup required for the job
    }

    @Override
    public String getName() {
        return JOB_NAME;
    }

    @Override
    public void setBlackboard(Blackboard<Object> blackboard) {
        this.blackboard = Objects.requireNonNull(blackboard);
    }
}