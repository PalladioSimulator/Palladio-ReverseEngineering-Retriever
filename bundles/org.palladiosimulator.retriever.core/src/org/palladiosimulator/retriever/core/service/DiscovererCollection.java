package org.palladiosimulator.retriever.core.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.palladiosimulator.retriever.services.Discoverer;
import org.palladiosimulator.retriever.services.ServiceCollection;

public class DiscovererCollection implements ServiceCollection<Discoverer> {
    public static final String EXTENSION_POINT = "org.palladiosimulator.retriever.services.discoverer";
    private final Set<Discoverer> discoverer = new HashSet<>();

    public DiscovererCollection() throws CoreException, InvalidRegistryObjectException {
        for (final IConfigurationElement extension : Platform.getExtensionRegistry()
            .getConfigurationElementsFor(EXTENSION_POINT)) {
            final Object o = extension.createExecutableExtension("class");
            if (o instanceof Discoverer) {
                this.discoverer.add((Discoverer) o);
            }
        }
    }

    @Override
    public Set<Discoverer> getServices() {
        return Collections.unmodifiableSet(this.discoverer);
    }
}
