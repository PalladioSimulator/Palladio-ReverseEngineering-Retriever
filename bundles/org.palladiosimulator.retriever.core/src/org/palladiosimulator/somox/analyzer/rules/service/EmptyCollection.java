package org.palladiosimulator.somox.analyzer.rules.service;

import java.util.Collections;
import java.util.Set;

import org.palladiosimulator.somox.analyzer.rules.engine.ServiceCollection;

public class EmptyCollection<T> implements ServiceCollection<T> {

    @Override
    public Set<T> getServices() {
        return Collections.emptySet();
    }

}
