package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class RequirementsBuilder {
    private final List<EntireInterface> requirements = new LinkedList<>();

    public void add(EntireInterface... interfaces) {
        this.add(List.of(interfaces));
    }

    public void add(Collection<EntireInterface> interfaces) {
        requirements.addAll(interfaces);
    }

    public Requirements create(Collection<OperationInterface> allDependencies) {
        return new Requirements(requirements, allDependencies);
    }

    public List<OperationInterface> toList() {
        return Collections.unmodifiableList(requirements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requirements);
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
        return Objects.equals(requirements, other.requirements);
    }
}
