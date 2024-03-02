package org.palladiosimulator.retriever.extraction.commonalities;

import java.util.Map;
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
}
