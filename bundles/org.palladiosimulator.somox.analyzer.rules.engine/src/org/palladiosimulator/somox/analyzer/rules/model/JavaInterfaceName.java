package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.List;
import java.util.Objects;

public class JavaInterfaceName implements InterfaceName {
    private final String name;

    public JavaInterfaceName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getInterfaces() {
        return List.of(name);
    }

    @Override
    public InterfaceName createInterface(String name) {
        return new JavaInterfaceName(name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
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
        JavaInterfaceName other = (JavaInterfaceName) obj;
        return Objects.equals(name, other.name);
    }
}