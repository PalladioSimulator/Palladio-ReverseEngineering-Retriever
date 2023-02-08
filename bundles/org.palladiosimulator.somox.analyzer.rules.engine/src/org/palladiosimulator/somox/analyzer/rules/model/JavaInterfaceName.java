package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.List;

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
}