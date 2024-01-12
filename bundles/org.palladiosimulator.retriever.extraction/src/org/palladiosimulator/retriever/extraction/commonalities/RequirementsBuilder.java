package org.palladiosimulator.retriever.extraction.commonalities;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class RequirementsBuilder {
    private final List<OperationInterface> requirements = new LinkedList<>();
    private final Set<OperationInterface> weakRequirements = new HashSet<>();

    public void add(final OperationInterface... interfaces) {
        this.add(List.of(interfaces));
    }

    public void add(final Collection<OperationInterface> interfaces) {
        this.requirements.addAll(interfaces);
    }

    public void addWeakly(final OperationInterface iface) {
        this.weakRequirements.add(iface);
    }

    public void strengthenIfPresent(final OperationInterface iface) {
        if (this.weakRequirements.contains(iface)) {
            this.weakRequirements.remove(iface);
            this.requirements.add(iface);
        }
    }

    public Requirements create(final Collection<OperationInterface> allDependencies,
            final Collection<OperationInterface> visibleProvisions) {
        return new Requirements(this.requirements, allDependencies, visibleProvisions);
    }

    public List<OperationInterface> toList() {
        return Collections.unmodifiableList(this.requirements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.requirements, this.weakRequirements);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (this.getClass() != obj.getClass())) {
            return false;
        }
        final RequirementsBuilder other = (RequirementsBuilder) obj;
        return Objects.equals(this.requirements, other.requirements)
                && Objects.equals(this.weakRequirements, other.weakRequirements);
    }
}
