package org.palladiosimulator.retriever.core.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.palladiosimulator.retriever.extraction.engine.ServiceCollection;

public class AnalystCollection implements ServiceCollection<Analyst> {
    public static final String EXTENSION_POINT = "org.palladiosimulator.somox.analyzer.rules.analyst";
    private final Set<Analyst> analysts = new HashSet<>();

    public AnalystCollection() throws CoreException {
        for (IConfigurationElement extension : Platform.getExtensionRegistry()
            .getConfigurationElementsFor(EXTENSION_POINT)) {
            final Object o = extension.createExecutableExtension("class");
            if (o instanceof Analyst) {
                analysts.add((Analyst) o);
            }
        }
    }

    @Override
    public Set<Analyst> getServices() {
        return Collections.unmodifiableSet(analysts);
    }
}
