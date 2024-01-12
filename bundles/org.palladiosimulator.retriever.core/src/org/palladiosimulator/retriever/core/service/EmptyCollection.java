package org.palladiosimulator.retriever.core.service;

import java.util.Collections;
import java.util.Set;

import org.palladiosimulator.retriever.extraction.engine.ServiceCollection;

public class EmptyCollection<T> implements ServiceCollection<T> {

    @Override
    public Set<T> getServices() {
        return Collections.emptySet();
    }

}
