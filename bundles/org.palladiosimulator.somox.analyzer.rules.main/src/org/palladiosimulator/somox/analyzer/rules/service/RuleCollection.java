package org.palladiosimulator.somox.analyzer.rules.service;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.palladiosimulator.somox.analyzer.rules.engine.IRule;
import org.palladiosimulator.somox.analyzer.rules.engine.ServiceCollection;

public class RuleCollection implements ServiceCollection<IRule> {
    public static final String EXTENSION_POINT = "org.palladiosimulator.somox.analyzer.rule";
    private Set<IRule> rules = new HashSet<>();

    public RuleCollection() throws CoreException {
        for (IConfigurationElement extension : Platform.getExtensionRegistry()
            .getConfigurationElementsFor(EXTENSION_POINT)) {
            final Object o = extension.createExecutableExtension("class");
            if (o instanceof IRule) {
                rules.add((IRule) o);
            }
        }
    }

    @Override
    public Set<IRule> getServices() {
        return rules;
    }
}
