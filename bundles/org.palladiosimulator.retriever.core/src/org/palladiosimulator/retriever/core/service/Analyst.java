package org.palladiosimulator.retriever.core.service;

import java.util.Set;

import org.palladiosimulator.retriever.extraction.engine.Service;

/**
 * The defining interface of the org.palladiosimulator.somox.analyzer.rules.analyst extension point.
 * Implement this interface to extend the rule engine by an additional analyst that can then process
 * the generated model.
 *
 * @author Florian Bossert
 */
public interface Analyst extends Service {
    @Override
    default Set<String> getDependentServices() {
        return Set.of();
    }
}
