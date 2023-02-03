package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ProvisionsBuilder {
    private final List<OperationInterface> provisions = new LinkedList<>();

    public void add(OperationInterface... provisions) {
        this.add(List.of(provisions));
    }

    public void add(Collection<OperationInterface> provisions) {
        this.provisions.addAll(provisions);
    }

    public Provisions create() {
        return new Provisions(provisions);
    }
}
