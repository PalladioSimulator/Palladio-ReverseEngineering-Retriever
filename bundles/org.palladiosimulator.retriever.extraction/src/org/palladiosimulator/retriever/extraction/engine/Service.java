package org.palladiosimulator.retriever.extraction.engine;

import java.util.Set;

import org.palladiosimulator.retriever.extraction.blackboard.RetrieverBlackboard;

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
     */
    Set<String> getRequiredServices();

    /**
     * IDs of services that may only run after this one.
     */
    Set<String> getDependentServices();
}
