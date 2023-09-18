package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

public class RESTName implements InterfaceName, OperationName {
    private final List<String> path;
    private final Optional<HTTPMethod> httpMethod;

    public RESTName(String path, Optional<HTTPMethod> httpMethod) throws IllegalArgumentException {
        this.httpMethod = httpMethod;
        Optional<List<String>> parsedPath = parsePath(path);
        if (parsedPath.isEmpty()) {
            throw new IllegalArgumentException("Could not parse path due to illegal format: \"" + path + "\"");
        }
        this.path = parsedPath.get();
    }

    @Override
    public String getName() {
        return getInterface();
    }

    @Override
    public String getInterface() {
        return toString();
    }

    @Override
    public Optional<String> forInterface(String baseInterface) {
        if (!isPartOf(baseInterface)) {
            return Optional.empty();
        }

        return Optional.of(getInterface());
    }

    @Override
    public List<String> getInterfaces() {
        Stack<List<String>> prefixes = new Stack<>();

        if (path.size() > 0) {
            prefixes.push(List.of(path.get(0)));
            for (int i = 1; i < path.size(); i++) {
                List<String> prefix = new ArrayList<>(prefixes.peek());
                prefix.add(path.get(i));
                prefixes.push(prefix);
            }
        }

        List<String> interfaces = new ArrayList<>(prefixes.size());

        if (httpMethod.isPresent()) {
            interfaces.add(getInterface());
        }

        // Insert the prefixes in reverse since the most specific element is at index 0 there.
        while (!prefixes.empty()) {
            interfaces.add(toName(prefixes.pop()));
        }

        // Always add root interface
        interfaces.add(toName(List.of()));

        return interfaces;
    }

    @Override
    public InterfaceName createInterface(String name) {
        return new RESTName(name, Optional.empty());
    }

    private static String toName(List<String> path) {
        StringBuilder name = new StringBuilder();
        name.append("/");
        for (int i = 0; i < path.size(); i++) {
            name.append(path.get(i));
            if (i + 1 < path.size()) {
                name.append("/");
            }
        }
        return name.toString();
    }

    @Override
    public String toString() {
        if (httpMethod.isPresent()) {
            return toName(path) + "[" + httpMethod.get()
                .toString() + "]";
        } else {
            return toName(path);
        }
    }

    private Optional<List<String>> parsePath(String string) {
        if (string.equals("/")) {
            return Optional.of(List.of());
        }
        String[] segments = string.split("/");
        if (segments.length <= 1) {
            return Optional.empty();
        }

        // Require an absolute path.
        if (!segments[0].isEmpty()) {
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
        return Objects.hash(path);
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
        RESTName other = (RESTName) obj;
        return Objects.equals(path, other.path);
    }

    @Override
    public boolean isPartOf(String iface) {
        String[] parts = iface.split("\\[");
        Optional<List<String>> interfacePathOption = parsePath(parts[0]);
        if (interfacePathOption.isEmpty()) {
            return false;
        }
        List<String> interfacePath = interfacePathOption.get();

        if (interfacePath.size() > path.size()) {
            return false;
        }

        for (int i = 0; i < interfacePath.size(); i++) {
            if (!path.get(i)
                .equals(interfacePath.get(i))) {
                return false;
            }
        }

        Optional<HTTPMethod> ifaceHttpMethod = Optional.empty();
        if (parts.length > 1) {
            // Assume that a '[' implies a ']'.
            int end = parts[1].lastIndexOf(']');
            String httpMethodName = parts[1].substring(0, end);
            ifaceHttpMethod = Optional.of(HTTPMethod.valueOf(httpMethodName));
        }

        if (interfacePath.size() == path.size() && ifaceHttpMethod.isPresent()
                && !ifaceHttpMethod.equals(this.httpMethod)) {
            return false;
        }

        return true;
    }
}