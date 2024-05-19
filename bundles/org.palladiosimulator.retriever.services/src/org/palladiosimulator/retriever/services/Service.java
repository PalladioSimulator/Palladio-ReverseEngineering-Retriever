package org.palladiosimulator.retriever.services;

import java.util.Set;

import org.palladiosimulator.retriever.services.blackboard.RetrieverBlackboard;

import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;

/**
 * The defining interface for any plug-in style service for Retriever.
 *
 * @author Florian Bossert
 */
public interface Service {
    IBlackboardInteractingJob<RetrieverBlackboard> create(RetrieverConfiguration configuration,
            RetrieverBlackboard blackboard);

    Set<String> getConfigurationKeys();

    String getName();

    String getID();

    /**
     * IDs of services that must run before this one.
     * If the set contains {@code null}, this service will be dependent
     * on all other services within its {@code ServiceCollection}.
     */
    Set<String> getRequiredServices();

    /**
     * IDs of services that may only run after this one.
     */
    Set<String> getDependentServices();
}
