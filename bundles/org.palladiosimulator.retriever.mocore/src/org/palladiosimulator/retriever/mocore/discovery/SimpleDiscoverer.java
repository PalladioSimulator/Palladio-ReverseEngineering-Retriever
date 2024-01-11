package org.palladiosimulator.retriever.mocore.discovery;

import java.util.Set;

import tools.mdsd.mocore.framework.discovery.Discoverer;
import tools.mdsd.mocore.framework.surrogate.Replaceable;

/**
 * Realization of the {@link Discoverer abstract framework discoverer} without changes or adaptations in behavior.
 *
 * @param <T> the type of {@link Replaceable} the discoverer provides
 */
public class SimpleDiscoverer<T extends Replaceable> extends Discoverer<T> {
    public SimpleDiscoverer(Set<T> discoveries, Class<T> discoveryType) {
        super(discoveries, discoveryType);
    }
}
