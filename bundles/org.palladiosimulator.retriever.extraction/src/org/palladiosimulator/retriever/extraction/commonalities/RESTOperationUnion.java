package org.palladiosimulator.retriever.extraction.commonalities;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class RESTOperationUnion implements OperationInterface {
    private RESTOperationName name;

    public RESTOperationUnion(RESTOperationName name) {
        this.name = name;
    }

    @Override
    public Name getName() {
        return name;
    }

    @Override
    public Map<OperationInterface, Set<Operation>> simplified() {
        return Map.of(this, Set.of());
    }

    @Override
    public String getInterface() {
        return name.getInterface();
    }

    @Override
    public String toString() {
        return name.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
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
        RESTOperationUnion other = (RESTOperationUnion) obj;
        return Objects.equals(name, other.name);
    }

}
