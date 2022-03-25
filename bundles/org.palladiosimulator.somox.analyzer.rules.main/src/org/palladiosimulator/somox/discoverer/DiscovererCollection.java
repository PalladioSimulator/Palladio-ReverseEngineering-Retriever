package org.palladiosimulator.somox.discoverer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;

public class DiscovererCollection {
    public static final String EXTENSION_POINT = "org.palladiosimulator.somox.discoverer";
    private final Set<Discoverer> discoverer = new HashSet<>();

    public DiscovererCollection() throws CoreException, InvalidRegistryObjectException {
        for (final IConfigurationElement extension : Platform.getExtensionRegistry()
                .getConfigurationElementsFor(EXTENSION_POINT)) {
            final Object o = extension.createExecutableExtension("class");
            if (o instanceof Discoverer) {
                discoverer.add((Discoverer) o);
            }
        }
    }

    public Set<Discoverer> getDiscoverer() {
        return Collections.unmodifiableSet(discoverer);
    }
}
