package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.Optional;

public class JavaName implements OperationName {
    private final String iface;
    private final String method;

    public JavaName(String iface, String method) {
        this.iface = iface;
        this.method = method;
    }

    @Override
    public String getFullName() {
        return iface + "#" + method;
    }

    @Override
    public Optional<String> getName(String baseInterface) {
        if (!iface.equals(baseInterface)) {
            return Optional.empty();
        }
        return Optional.of(method);
    }

    @Override
    public boolean isPartOf(String iface) {
        return this.iface.equals(iface);
    }
}