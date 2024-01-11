package org.palladiosimulator.somox.analyzer.rules.mocore.discovery;

import java.util.Collection;

import tools.mdsd.mocore.framework.discovery.Discoverer;

/**
 * Represents an entity able to deconstruct a source of model-based information like existing models or configuration
 * files into model-elements and model-relations.
 *
 * @param <T> the type of model or information source deconstructed by the decompositor
 */
public interface Decompositor<T> {
    /**
     * Extracts model-elements and model-relations from a source of model-based information.
     *
     * @param source the source of model-based information
     * @return a collection of discoverers containing the extracted elements and relations
     */
    Collection<Discoverer<?>> decompose(T source);
}
