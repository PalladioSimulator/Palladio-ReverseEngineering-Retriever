package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.palladiosimulator.somox.analyzer.rules.engine.MapMerger;

public class Requirements implements Iterable<EntireInterface> {
    private final Set<EntireInterface> requirements;

    public Requirements(Collection<EntireInterface> requiredInterfaces) {
        this.requirements = new HashSet<>(requiredInterfaces);
    }

    public Set<EntireInterface> get() {
        return Collections.unmodifiableSet(requirements);
    }

    public boolean contains(OperationInterface iface) {
        return requirements.stream()
            .anyMatch(x -> x.isPartOf(iface));
    }

    @Override
    public Iterator<EntireInterface> iterator() {
        return Collections.unmodifiableCollection(requirements)
            .iterator();
    }

    public Map<String, List<IMethodBinding>> simplified() {
        List<Map<String, List<IMethodBinding>>> simplifiedInterfaces = requirements.stream()
            .map(OperationInterface::simplified)
            .collect(Collectors.toList());

        return MapMerger.merge(simplifiedInterfaces);
    }
}
