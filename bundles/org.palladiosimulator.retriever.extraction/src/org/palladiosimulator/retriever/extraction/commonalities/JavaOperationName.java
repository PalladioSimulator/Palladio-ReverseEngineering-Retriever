package org.palladiosimulator.retriever.extraction.commonalities;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class JavaOperationName implements OperationName {
    private final String iface;
    private final String method;

    public JavaOperationName(final String iface, final String method) {
        this.iface = iface;
        this.method = method;
    }

    @Override
    public Optional<String> forInterface(final String baseInterface) {
        if (!this.iface.equals(baseInterface) && !baseInterface.startsWith(this.iface + "#")) {
            return Optional.empty();
        }
        return Optional.of(this.method);
    }

    @Override
    public List<String> getInterfaces() {
        return List.of(this.toString(), this.iface);
    }

    @Override
    public String getInterface() {
        return this.iface;
    }

    @Override
    public InterfaceName createInterface(final String name) {
        return new JavaInterfaceName(name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.iface, this.method);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (this.getClass() != obj.getClass())) {
            return false;
        }
        final JavaOperationName other = (JavaOperationName) obj;
        return Objects.equals(this.iface, other.iface) && Objects.equals(this.method, other.method);
    }

    @Override
    public String toString() {
        return this.iface + "#" + this.method;
    }
}