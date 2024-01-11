package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class JavaOperationName implements OperationName {
    private final String iface;
    private final String method;

    public JavaOperationName(String iface, String method) {
        this.iface = iface;
        this.method = method;
    }

    @Override
    public Optional<String> forInterface(String baseInterface) {
		if (!iface.equals(baseInterface) && !baseInterface.startsWith(iface + "#")) {
            return Optional.empty();
        }
        return Optional.of(method);
    }

    @Override
    public List<String> getInterfaces() {
        return List.of(toString(), iface);
    }

    @Override
    public String getInterface() {
        return iface;
    }

    @Override
    public InterfaceName createInterface(String name) {
        return new JavaInterfaceName(name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iface, method);
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
        JavaOperationName other = (JavaOperationName) obj;
        return Objects.equals(iface, other.iface) && Objects.equals(method, other.method);
    }

    @Override
    public String toString() {
        return iface + "#" + method;
    }
}