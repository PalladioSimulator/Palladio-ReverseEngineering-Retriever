package org.palladiosimulator.retriever.extraction.commonalities;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface Name {
    InterfaceName createInterface(String name);

    /**
     * @returns interfaces that this name is part of, sorted from specific to general.
     */
    List<String> getInterfaces();

    /**
     * @returns the most specific common interface
     */
    default Optional<String> getCommonInterface(Name other) {
        Set<String> interfaces = new HashSet<>(getInterfaces());
        for (String iface : other.getInterfaces()) {
            if (interfaces.contains(iface)) {
                return Optional.of(iface);
            }
        }
        return Optional.empty();
    }

    default boolean isPartOf(String iface) {
        return getInterfaces().contains(iface);
    }
}
