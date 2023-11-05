package org.palladiosimulator.somox.analyzer.rules.service;

import org.palladiosimulator.somox.analyzer.rules.engine.Service;

/**
 * The defining interface of the org.palladiosimulator.somox.analyzer.rules.analyst extension point.
 * Implement this interface to extend the rule engine by an additional analyst that can then process
 * the generated model.
 *
 * @author Florian Bossert
 */
public interface Analyst extends Service {
}
