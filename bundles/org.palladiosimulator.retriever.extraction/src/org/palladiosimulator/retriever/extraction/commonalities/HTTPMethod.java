package org.palladiosimulator.retriever.extraction.commonalities;

import java.util.Collection;
import java.util.Set;

public enum HTTPMethod {
    GET, POST, PUT, DELETE, PATCH;

    public static boolean areAllPresent(Collection<HTTPMethod> httpMethods) {
        return httpMethods.containsAll(all());
    }

    public static Set<HTTPMethod> all() {
        return Set.of(values());
    }

    public static Set<HTTPMethod> any() {
        return Set.of();
    }
}
