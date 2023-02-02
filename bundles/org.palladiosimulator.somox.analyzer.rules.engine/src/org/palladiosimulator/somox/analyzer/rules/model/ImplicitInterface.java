package org.palladiosimulator.somox.analyzer.rules.model;

public class ImplicitInterface implements Provision {
    private final String name;

    public ImplicitInterface(String name) {
        this.name = name;
    }

    public String getInterface() {
        return name;
    }

    @Override
    public boolean isPartOf(String baseInterface) {
        return name.equals(baseInterface);
    }
}
