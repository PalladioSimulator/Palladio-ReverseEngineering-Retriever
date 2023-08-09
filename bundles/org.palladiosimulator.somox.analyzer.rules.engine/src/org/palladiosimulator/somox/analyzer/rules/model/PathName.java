package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

public class PathName implements InterfaceName, OperationName {
    private final List<String> path;

    public PathName(String path) throws IllegalArgumentException {
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
        return toName(path);
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

        // Insert the prefixes in reverse since the most specific element is at index 0 there.
        while (!prefixes.empty()) {
            interfaces.add(toName(prefixes.pop()));
        }

        return interfaces;
    }

    @Override
    public InterfaceName createInterface(String name) {
        return new PathName(name);
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
        return toName(path);
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
        PathName other = (PathName) obj;
        return Objects.equals(path, other.path);
    }

    @Override
    public boolean isPartOf(String iface) {
        Optional<List<String>> interfacePathOption = parsePath(iface);
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

        return true;
    }
}