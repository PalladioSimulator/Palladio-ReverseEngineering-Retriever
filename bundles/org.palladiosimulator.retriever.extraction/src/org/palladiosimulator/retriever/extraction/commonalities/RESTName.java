package org.palladiosimulator.retriever.extraction.commonalities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

public class RESTName implements InterfaceName, OperationName {
    private final String host;
    private final List<String> path;
    private final Optional<HTTPMethod> httpMethod;

    public RESTName(final String host, final String path, final Optional<HTTPMethod> httpMethod)
            throws IllegalArgumentException {
        this.host = host;
        this.httpMethod = httpMethod;
        final Optional<List<String>> parsedPath = this.parsePath(host + path);
        if (parsedPath.isEmpty()) {
            throw new IllegalArgumentException("Could not parse path due to illegal format: \"" + path + "\"");
        }
        this.path = parsedPath.get();

        // Keep host name separate
        this.path.remove(0);
    }

    @Override
    public String getName() {
        return this.getInterface();
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
        final Stack<List<String>> prefixes = new Stack<>();

        if (this.path.size() > 0) {
            prefixes.push(List.of(this.path.get(0)));
            for (int i = 1; i < this.path.size(); i++) {
                final List<String> prefix = new ArrayList<>(prefixes.peek());
                prefix.add(this.path.get(i));
                prefixes.push(prefix);
            }
        }

        final List<String> interfaces = new ArrayList<>(prefixes.size());

        if (this.httpMethod.isPresent()) {
            interfaces.add(this.getInterface());
        }

        // Insert the prefixes in reverse since the most specific element is at index 0 there.
        while (!prefixes.empty()) {
            interfaces.add(this.toName(prefixes.pop()));
        }

        // Always add root interface
        interfaces.add(this.toName(List.of()));

        return interfaces;
    }

    @Override
    public InterfaceName createInterface(final String name) {
        return new RESTName(this.host, name, Optional.empty());
    }

    private String toName(final List<String> path) {
        final StringBuilder name = new StringBuilder();
        name.append("/");
        for (int i = 0; i < path.size(); i++) {
            name.append(path.get(i));
            if (i + 1 < path.size()) {
                name.append("/");
            }
        }
        return this.host + name.toString();
    }

    @Override
    public String toString() {
        final String pathString = this.toName(this.path);
        String methodString = "";
        if (this.httpMethod.isPresent()) {
            methodString = "[" + this.httpMethod.get()
                .toString() + "]";
        }
        return pathString + methodString;
    }

    private Optional<List<String>> parsePath(final String string) {
        final String[] segments = string.split("/");

        // Require at least a "/" after the host name
        if (segments.length == 1 && !string.endsWith("/")) {
            return Optional.empty();
        }

        // Remove empty segments.
        return Optional.of(List.of(segments)
            .stream()
            .filter(x -> !x.isEmpty())
            .collect(Collectors.toList()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.host, this.path, this.httpMethod);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (this.getClass() != obj.getClass())) {
            return false;
        }
        final RESTName other = (RESTName) obj;
        return Objects.equals(this.host, other.host) && Objects.equals(this.path, other.path)
                && Objects.equals(this.httpMethod, other.httpMethod);
    }

    @Override
    public boolean isPartOf(final String iface) {
        final String[] parts = iface.split("\\[");
        final Optional<List<String>> interfacePathOption = this.parsePath(parts[0]);
        if (interfacePathOption.isEmpty()) {
            return false;
        }
        final List<String> interfacePath = interfacePathOption.get();
        final String otherHost = interfacePath.remove(0);

        if (!otherHost.equals(this.host) || (interfacePath.size() > this.path.size())) {
            return false;
        }

        for (int i = 0; i < interfacePath.size(); i++) {
            if (!this.path.get(i)
                .equals(interfacePath.get(i))) {
                return false;
            }
        }

        Optional<HTTPMethod> ifaceHttpMethod = Optional.empty();
        if (parts.length > 1) {
            // Assume that a '[' implies a ']'.
            final int end = parts[1].lastIndexOf(']');
            final String httpMethodName = parts[1].substring(0, end);
            ifaceHttpMethod = Optional.of(HTTPMethod.valueOf(httpMethodName));
        }

        // TODO: If this.httpMethod.isEmpty(), see it as part of the other interface anyway.
        // This allows some imprecision from ECMAScript detection
        if (interfacePath.size() == this.path.size() && ifaceHttpMethod.isPresent() && this.httpMethod.isPresent()
                && !ifaceHttpMethod.equals(this.httpMethod)) {
            return false;
        }

        return true;
    }
}