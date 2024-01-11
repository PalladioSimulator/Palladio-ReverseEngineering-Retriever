package org.palladiosimulator.somox.analyzer.rules.workflow;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.log4j.Logger;
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
    private static final Logger LOG = Logger.getLogger(SeffMergerJob.class);

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

            // Assumes that each component from source repository has a counterpart with the same
            // name in destination
            // repository. Otherwise, exception is thrown.
            BasicComponent sourceComponent = (BasicComponent) component;
            Optional<BasicComponent> destinationComponentOption = destinationRepository.getComponents__Repository()
                .stream()
                .filter(otherComponent -> otherComponent.getEntityName()
                    .equals(sourceComponent.getEntityName()))
                .filter(BasicComponent.class::isInstance)
                .map(BasicComponent.class::cast)
                .findFirst();

            if (destinationComponentOption.isEmpty()) {
                LOG.warn("Failed to find destination component " + sourceComponent.getEntityName() + "!");
                continue;
            }
            BasicComponent destinationComponent = destinationComponentOption.get();

            // Overwrite seffs within destination component
            List<ServiceEffectSpecification> sourceSeffs = List
                .copyOf(sourceComponent.getServiceEffectSpecifications__BasicComponent());
            for (ServiceEffectSpecification sourceSeff : sourceSeffs) {
                // Retrieve destination signature for seff, throw if signature is not provided by
                // destination component
                Optional<OperationSignature> destinationSignatureOption = destinationComponent
                    .getProvidedRoles_InterfaceProvidingEntity()
                    .stream()
                    .filter(role -> role instanceof OperationProvidedRole)
                    .map(role -> (OperationProvidedRole) role)
                    .flatMap(role -> role.getProvidedInterface__OperationProvidedRole()
                        .getSignatures__OperationInterface()
                        .stream())
                    .filter(signature -> signature.getEntityName()
                        .equals(sourceSeff.getDescribedService__SEFF()
                            .getEntityName()))
                    .findFirst();

                if (destinationSignatureOption.isEmpty()) {
                    LOG.warn("Failed to find destination signature for " + sourceSeff.getDescribedService__SEFF()
                        .getEntityName() + " in component " + destinationComponent.getEntityName() + "!");
                    continue;
                }
                OperationSignature destinationSignature = destinationSignatureOption.get();

                // Set component and signature of source seff to destination elements
                sourceSeff.setBasicComponent_ServiceEffectSpecification(destinationComponent);
                sourceSeff.setDescribedService__SEFF(destinationSignature);

                // Adapt external call actions to new repository -> Swap signatures and required
                // roles
                EList<AbstractAction> behaviorSteps = ((ResourceDemandingSEFF) sourceSeff).getSteps_Behaviour();
                for (AbstractAction action : behaviorSteps) {
                    if (!(action instanceof ExternalCallAction)) {
                        continue;
                    }
                    ExternalCallAction externalCallAction = (ExternalCallAction) action;
                    String calledSignatureEntityName = externalCallAction.getCalledService_ExternalService()
                        .getEntityName();

                    // Fetch called signature from destination repository
                    Optional<OperationSignature> calledSignatureOption = destinationRepository
                        .getInterfaces__Repository()
                        .stream()
                        .filter(interFace -> interFace instanceof OperationInterface)
                        .flatMap(interFace -> ((OperationInterface) interFace).getSignatures__OperationInterface()
                            .stream())
                        .filter(signature -> signature.getEntityName()
                            .equals(calledSignatureEntityName))
                        .findFirst();

                    if (calledSignatureOption.isEmpty()) {
                        LOG.warn("Failed to find called signature for " + calledSignatureEntityName + "!");
                        continue;
                    }
                    OperationSignature calledSignature = calledSignatureOption.get();

                    // Fetch required role from destination repository
                    Optional<OperationRequiredRole> requiredRoleOption = destinationComponent
                        .getRequiredRoles_InterfaceRequiringEntity()
                        .stream()
                        .filter(role -> role instanceof OperationRequiredRole)
                        .map(role -> (OperationRequiredRole) role)
                        .filter(role -> role.getRequiredInterface__OperationRequiredRole()
                            .getSignatures__OperationInterface()
                            .contains(calledSignature))
                        .findFirst();

                    if (requiredRoleOption.isEmpty()) {
                        LOG.warn(
                                "Failed to find required role for " + calledSignature.getInterface__OperationSignature()
                                    .getEntityName() + "#" + calledSignature.getEntityName() + "!");
                        continue;
                    }
                    OperationRequiredRole requiredRole = requiredRoleOption.get();

                    externalCallAction.setCalledService_ExternalService(calledSignature);
                    externalCallAction.setRole_ExternalService(requiredRole);
                }

                // Find optional already existing and conflicting seff in destination component
                Optional<ServiceEffectSpecification> optionalDestinationSeff = destinationComponent
                    .getServiceEffectSpecifications__BasicComponent()
                    .stream()
                    .filter(destinationSeff -> destinationSeff.getDescribedService__SEFF()
                        .getEntityName()
                        .equals(sourceSeff.getDescribedService__SEFF()
                            .getEntityName()))
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
