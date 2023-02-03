package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.Optional;

public class PathName implements OperationName {
    private final String path;

    public PathName(String path) {
        this.path = path;
    }

    @Override
    public String getFullName() {
        return path;
    }

    @Override
    public Optional<String> forInterface(String baseInterface) {
        if (!path.startsWith(baseInterface)) {
            return Optional.empty();
        }
        return Optional.of(path.substring(baseInterface.length()));
    }

    @Override
    public String getInterface() {
        return path;
    }

    // TODO: How are path separators '/' handled?
    // /page/info is not a parent of /page/information/user.
    @Override
    public boolean isPartOf(String iface) {
        return path.startsWith(iface);
    }
}