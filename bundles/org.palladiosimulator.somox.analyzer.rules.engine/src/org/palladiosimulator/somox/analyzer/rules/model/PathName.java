package org.palladiosimulator.somox.analyzer.rules.model;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;

public class PathName implements InterfaceName, OperationName {
    private final Path path;

    public PathName(String path) {
        this.path = Path.of(cutOffWildcard(path));
    }

    @Override
    public String getName() {
        return toName(path);
    }

    @Override
    public String getFullName() {
        return toName(path);
    }

    @Override
    public String getInterface() {
        return toName(path);
    }

    @Override
    public Optional<String> forInterface(String baseInterface) {
        Path interfacePath;
        try {
            interfacePath = Path.of(cutOffWildcard(baseInterface));
        } catch (InvalidPathException e) {
            return Optional.empty();
        }

        if (!path.startsWith(interfacePath)) {
            return Optional.empty();
        }

        return Optional.of(toName(interfacePath.relativize(path)));
    }

    @Override
    public List<String> getInterfaces() {
        Stack<Path> prefixes = new Stack<>();

        prefixes.push(path.getRoot());
        for (Path segment : path) {
            prefixes.push(prefixes.peek()
                .resolve(segment));
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

    private static String toName(Path path) {
        // Result in the same paths on Windows as on other operating systems.
        return path.toString()
            .replace('\\', '/');
    }

    private static String cutOffWildcard(String path) {
        int wildcardIndex = -1;
        if (path.contains("*")) {
            wildcardIndex = path.indexOf('*');
        }
        if (path.contains("{")) {
            if (wildcardIndex > -1) {
                wildcardIndex = Math.min(wildcardIndex, path.indexOf('{'));
            } else {
                wildcardIndex = path.indexOf('{');
            }
        }
        if (wildcardIndex > -1) {
            return path.substring(0, wildcardIndex);
        } else {
            return path;
        }
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
}