package org.palladiosimulator.retriever.extraction.commonalities;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ProvisionsBuilder {
    private final List<OperationInterface> provisions = new LinkedList<>();
    private final Set<OperationInterface> weakProvisions = new HashSet<>();

    public void add(final OperationInterface... provisions) {
        this.add(List.of(provisions));
    }

    public synchronized void add(final Collection<OperationInterface> provisions) {
        this.provisions.addAll(provisions);
    }

    public synchronized void addWeakly(final OperationInterface iface) {
        this.weakProvisions.add(iface);
    }

    public synchronized void strengthenIfPresent(final OperationInterface iface) {
        List<OperationInterface> remainingProvisions = new LinkedList<>();
        for (final OperationInterface provision : this.weakProvisions) {
            final boolean partlyProvided = provision.isPartOf(iface);
            final boolean entirelyProvided = iface.isPartOf(provision);
            if (partlyProvided || entirelyProvided) {
                this.provisions.add(provision);
            } else {
                remainingProvisions.add(provision);
            }
        }
        this.weakProvisions.clear();
        this.weakProvisions.addAll(remainingProvisions);
    }

    public synchronized boolean containsRelated(final OperationInterface requirement) {
        for (final OperationInterface provision : this.provisions) {
            final boolean partlyProvided = provision.isPartOf(requirement);
            final boolean entirelyProvided = requirement.isPartOf(provision);
            if (partlyProvided || entirelyProvided) {
                return true;
            }
        }

        for (final OperationInterface provision : this.weakProvisions) {
            final boolean partlyProvided = provision.isPartOf(requirement);
            final boolean entirelyProvided = requirement.isPartOf(provision);
            if (partlyProvided || entirelyProvided) {
                return true;
            }
        }

        return false;
    }

    public Provisions create(final Collection<OperationInterface> allDependencies) {
        return new Provisions(this.provisions, allDependencies);
    }

    public List<OperationInterface> toList() {
        return Collections.unmodifiableList(this.provisions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.provisions, this.weakProvisions);
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
        return Objects.equals(this.provisions, other.provisions)
                && Objects.equals(this.weakProvisions, other.weakProvisions);
    }
}
