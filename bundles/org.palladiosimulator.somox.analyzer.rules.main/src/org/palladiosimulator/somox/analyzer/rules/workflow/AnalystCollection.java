package org.palladiosimulator.somox.analyzer.rules.workflow;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

public class AnalystCollection {
    public static final String EXTENSION_POINT = "org.palladiosimulator.somox.analyzer.rules.analyst";
    private Set<Analyst> analysts = new HashSet<>();
    
    public AnalystCollection() throws CoreException {
        for (IConfigurationElement extension : Platform.getExtensionRegistry()
            .getConfigurationElementsFor(EXTENSION_POINT)) {
            final Object o = extension.createExecutableExtension("class");
            if (o instanceof Analyst) {
                analysts.add((Analyst) o);
            }
        }
    }
    
    public Set<Analyst> getAnalysts() {
        return Collections.unmodifiableSet(analysts);
    }
}
