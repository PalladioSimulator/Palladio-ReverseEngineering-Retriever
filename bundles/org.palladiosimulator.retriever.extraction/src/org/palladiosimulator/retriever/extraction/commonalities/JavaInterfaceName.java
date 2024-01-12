package org.palladiosimulator.retriever.extraction.commonalities;

import java.util.List;
import java.util.Objects;

public class JavaInterfaceName implements InterfaceName {
    private final String name;

    public JavaInterfaceName(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public List<String> getInterfaces() {
        return List.of(this.name);
    }

    @Override
    public InterfaceName createInterface(final String name) {
        return new JavaInterfaceName(name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (this.getClass() != obj.getClass())) {
            return false;
        }
        final JavaInterfaceName other = (JavaInterfaceName) obj;
        return Objects.equals(this.name, other.name);
    }

    @Override
    public String toString() {
        return this.name;
    }
}