package org.palladiosimulator.somox.analyzer.rules.model;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

public class PathName implements InterfaceName, OperationName {
    private final Path path;

    public PathName(String path) {
        this.path = Path.of(path);
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
            interfacePath = Path.of(baseInterface);
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

    private static String toName(Path path) {
        // Result in the same paths on Windows as on other operating systems.
        return path.toString()
            .replace('\\', '/');
    }

    @Override
    public InterfaceName createInterface(String name) {
        return new PathName(name);
    }
}