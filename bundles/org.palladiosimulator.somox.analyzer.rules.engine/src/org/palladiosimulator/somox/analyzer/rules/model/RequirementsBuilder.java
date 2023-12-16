package org.palladiosimulator.somox.analyzer.rules.model;

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

    public void add(OperationInterface... interfaces) {
        this.add(List.of(interfaces));
    }

    public void add(Collection<OperationInterface> interfaces) {
        requirements.addAll(interfaces);
    }

    public void addWeakly(OperationInterface iface) {
        weakRequirements.add(iface);
    }

    public void strengthenIfPresent(OperationInterface iface) {
        if (weakRequirements.contains(iface)) {
            weakRequirements.remove(iface);
            requirements.add(iface);
        }
    }

    public Requirements create(Collection<OperationInterface> allDependencies,
            Collection<OperationInterface> visibleProvisions) {
        return new Requirements(requirements, allDependencies, visibleProvisions);
    }

    public List<OperationInterface> toList() {
        return Collections.unmodifiableList(requirements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requirements, weakRequirements);
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
        RequirementsBuilder other = (RequirementsBuilder) obj;
        return Objects.equals(requirements, other.requirements)
                && Objects.equals(weakRequirements, other.weakRequirements);
    }
}
