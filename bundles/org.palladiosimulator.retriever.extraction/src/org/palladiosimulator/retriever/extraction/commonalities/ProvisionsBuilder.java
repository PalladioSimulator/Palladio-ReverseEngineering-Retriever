package org.palladiosimulator.retriever.extraction.commonalities;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ProvisionsBuilder {
    private final List<OperationInterface> provisions = new LinkedList<>();

    public void add(OperationInterface... provisions) {
        this.add(List.of(provisions));
    }

    public void add(Collection<OperationInterface> provisions) {
        this.provisions.addAll(provisions);
    }

    public Provisions create(Collection<OperationInterface> allDependencies) {
        return new Provisions(provisions, allDependencies);
    }

    public List<OperationInterface> toList() {
        return Collections.unmodifiableList(provisions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(provisions);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ProvisionsBuilder other = (ProvisionsBuilder) obj;
        return Objects.equals(provisions, other.provisions);
    }
}
