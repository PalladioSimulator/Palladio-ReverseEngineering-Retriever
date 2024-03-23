package org.palladiosimulator.retriever.extraction.commonalities;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public enum HTTPMethod {
    GET, POST, PUT, DELETE, PATCH,
    /**
     * Special value signaling all HTTPMethods are provided/required.
     */
    WILDCARD;

    public static boolean areAllPresent(Collection<HTTPMethod> httpMethods) {
        if (httpMethods.contains(HTTPMethod.WILDCARD)) {
            return true;
        }

        Set<HTTPMethod> normalValues = new HashSet<>(Set.of(values()));
        normalValues.remove(WILDCARD);
        return httpMethods.containsAll(normalValues);
    }
}
