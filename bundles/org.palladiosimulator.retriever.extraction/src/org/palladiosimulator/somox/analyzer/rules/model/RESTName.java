package org.palladiosimulator.somox.analyzer.rules.model;

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

    public RESTName(String host, String path, Optional<HTTPMethod> httpMethod) throws IllegalArgumentException {
        this.host = host;
        this.httpMethod = httpMethod;
        Optional<List<String>> parsedPath = parsePath(host + path);
        if (parsedPath.isEmpty()) {
            throw new IllegalArgumentException("Could not parse path due to illegal format: \"" + path + "\"");
        }
        this.path = parsedPath.get();

        // Keep host name separate
        this.path.remove(0);
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
        return new RESTName(host, name, Optional.empty());
    }

    private String toName(List<String> path) {
        StringBuilder name = new StringBuilder();
        name.append("/");
        for (int i = 0; i < path.size(); i++) {
            name.append(path.get(i));
            if (i + 1 < path.size()) {
                name.append("/");
            }
        }
        return host + name.toString();
    }

    @Override
    public String toString() {
        String pathString = toName(path);
        String methodString = "";
        if (httpMethod.isPresent()) {
            methodString = "[" + httpMethod.get()
                .toString() + "]";
        }
        return pathString + methodString;
    }

    private Optional<List<String>> parsePath(String string) {
        String[] segments = string.split("/");

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
        return Objects.hash(host, path, httpMethod);
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
        return Objects.equals(host, other.host) && Objects.equals(path, other.path)
                && Objects.equals(httpMethod, other.httpMethod);
    }

    @Override
    public boolean isPartOf(String iface) {
        String[] parts = iface.split("\\[");
        Optional<List<String>> interfacePathOption = parsePath(parts[0]);
        if (interfacePathOption.isEmpty()) {
            return false;
        }
        List<String> interfacePath = interfacePathOption.get();
        String otherHost = interfacePath.remove(0);

        if (!otherHost.equals(host)) {
            return false;
        }

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

        // TODO: If this.httpMethod.isEmpty(), see it as part of the other interface anyway.
        // This allows some imprecision from ECMAScript detection
        if (interfacePath.size() == path.size() && ifaceHttpMethod.isPresent() && this.httpMethod.isPresent()
                && !ifaceHttpMethod.equals(this.httpMethod)) {
            return false;
        }

        return true;
    }
}