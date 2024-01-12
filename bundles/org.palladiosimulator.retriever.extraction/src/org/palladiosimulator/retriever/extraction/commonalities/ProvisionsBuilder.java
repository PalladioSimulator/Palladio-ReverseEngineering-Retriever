package org.palladiosimulator.retriever.extraction.commonalities;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ProvisionsBuilder {
    private final List<OperationInterface> provisions = new LinkedList<>();

    public void add(final OperationInterface... provisions) {
        this.add(List.of(provisions));
    }

    public void add(final Collection<OperationInterface> provisions) {
        this.provisions.addAll(provisions);
    }

    public Provisions create(final Collection<OperationInterface> allDependencies) {
        return new Provisions(this.provisions, allDependencies);
    }

    public List<OperationInterface> toList() {
        return Collections.unmodifiableList(this.provisions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.provisions);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (this.getClass() != obj.getClass())) {
            return false;
        }
        final ProvisionsBuilder other = (ProvisionsBuilder) obj;
        return Objects.equals(this.provisions, other.provisions);
    }
}
