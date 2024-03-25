package org.palladiosimulator.retriever.extraction.commonalities;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RESTOperationName implements OperationName {
    private RESTName restName;
    /**
     * This set must never be empty.
     */
    private final Set<HTTPMethod> httpMethods;

    public RESTOperationName(final String host, final String path, final Set<HTTPMethod> httpMethods)
            throws IllegalArgumentException {
        this(new RESTName(host, path), httpMethods);
    }

    public RESTOperationName(final String host, final String path, final HTTPMethod... httpMethods)
            throws IllegalArgumentException {
        this(host, path, Set.of(httpMethods));
    }

    private RESTOperationName(final RESTName restName, final Set<HTTPMethod> httpMethods) {
        this.restName = restName;
        if (httpMethods.isEmpty()) {
            this.httpMethods = Set.of(HTTPMethod.WILDCARD);
        } else {
            this.httpMethods = Collections.unmodifiableSet(httpMethods);
        }
    }

    @Override
    public String getInterface() {
        return this.toString();
    }

    @Override
    public Optional<String> forInterface(final String baseInterface) {
        if (!this.isPartOf(baseInterface)) {
            return Optional.empty();
        }

        return Optional.of(this.getInterface());
    }

    @Override
    public List<String> getInterfaces() {
        return restName.getInterfaces();
    }

    @Override
    public Name createName(final String name) {
        return RESTOperationName.parse(name)
            .orElseThrow();
    }

    @Override
    public String toString() {
        final String pathString = this.restName.toString();

        if (this.httpMethods.isEmpty() || HTTPMethod.areAllPresent(this.httpMethods)) {
            return pathString;
        }

        String httpMethodString = "";
        List<String> httpMethodNames = this.httpMethods.stream()
            .map(HTTPMethod::toString)
            .sorted()
            .collect(Collectors.toList());
        httpMethodString = "[";
        httpMethodString += String.join(",", httpMethodNames);
        httpMethodString += "]";

        return pathString + httpMethodString;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.restName, this.httpMethods);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (this.getClass() != obj.getClass())) {
            return false;
        }
        final RESTOperationName other = (RESTOperationName) obj;
        return Objects.equals(this.restName, other.restName) && Objects.equals(this.httpMethods, other.httpMethods);
    }

    @Override
    public Optional<String> getCommonInterface(final Name other) {
        if (other instanceof RESTOperationName otherREST) {
            if (this.restName.equals(otherREST.restName)) {
                Set<HTTPMethod> httpMethodUnion = new HashSet<>(this.httpMethods);
                httpMethodUnion.addAll(otherREST.httpMethods);
                return Optional.of(new RESTOperationName(this.restName, httpMethodUnion).toString());
            } else {
                return this.restName.getCommonInterface(otherREST.restName);
            }
        } else {
            return OperationName.super.getCommonInterface(other);
        }
    }

    @Override
    public boolean isPartOf(final String iface) {
        if (RESTName.parse(iface)
            .isPresent()) {
            return restName.isPartOf(iface);
        }

        Optional<RESTOperationName> parsedIface = parse(iface);
        if (parsedIface.isEmpty()) {
            return false;
        }

        RESTOperationName restIface = parsedIface.get();

        if (!this.restName.equals(restIface.restName)) {
            return false;
        }

        Set<HTTPMethod> normalHttpMethods = new HashSet<>(this.httpMethods);
        normalHttpMethods.remove(HTTPMethod.WILDCARD);

        if (!restIface.httpMethods.contains(HTTPMethod.WILDCARD)
                && !restIface.httpMethods.containsAll(this.httpMethods)) {
            return false;
        }
        return true;
    }

    public static Optional<RESTOperationName> parse(final String iface) {
        final String[] parts = iface.split("\\[");
        final Optional<RESTName> restNameOption = RESTName.parse(parts[0]);
        if (restNameOption.isEmpty()) {
            return Optional.empty();
        }

        Set<HTTPMethod> httpMethods = Set.of(HTTPMethod.WILDCARD);
        if (parts.length > 1) {
            // Assume that a '[' implies a ']'.
            final int end = parts[1].lastIndexOf(']');
            final String httpMethodNames = parts[1].substring(0, end);

            // Narrow the scope.
            httpMethods = Stream.of(httpMethodNames.split(","))
                .map(HTTPMethod::valueOf)
                .collect(Collectors.toSet());
        }

        return Optional.of(new RESTOperationName(restNameOption.get(), httpMethods));
    }
}