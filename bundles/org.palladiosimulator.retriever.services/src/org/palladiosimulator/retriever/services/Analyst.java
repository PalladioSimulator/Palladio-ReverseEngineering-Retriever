package org.palladiosimulator.retriever.services;

import java.util.Set;

/**
 * The defining interface of the org.palladiosimulator.retriever.extraction.analyst extension point.
 * Implement this interface to extend Retriever by an additional analyst that can then process the
 * generated model.
 *
 * @author Florian Bossert
 */
public interface Analyst extends Service {
    @Override
    default Set<String> getDependentServices() {
        return Set.of();
    }
}
