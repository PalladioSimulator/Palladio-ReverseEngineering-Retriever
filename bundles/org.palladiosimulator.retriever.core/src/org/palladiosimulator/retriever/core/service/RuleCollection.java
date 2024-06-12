package org.palladiosimulator.retriever.core.service;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.palladiosimulator.retriever.services.Rule;
import org.palladiosimulator.retriever.services.ServiceCollection;

public class RuleCollection implements ServiceCollection<Rule> {
    public static final String EXTENSION_POINT = "org.palladiosimulator.retriever.services.rule";
    private final Set<Rule> rules = new HashSet<>();

    public RuleCollection() throws CoreException {
        for (final IConfigurationElement extension : Platform.getExtensionRegistry()
            .getConfigurationElementsFor(EXTENSION_POINT)) {
            final Object o = extension.createExecutableExtension("class");
            if (o instanceof Rule) {
                this.rules.add((Rule) o);
            }
        }
    }

    @Override
    public Set<Rule> getServices() {
        return this.rules;
    }
}
