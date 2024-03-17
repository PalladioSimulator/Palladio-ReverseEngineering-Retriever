package org.palladiosimulator.retriever.extraction.engine;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class MapMerger {

    private MapMerger() {
        throw new IllegalStateException();
    }

    public static <K, V, C extends Collection<V>> Map<K, C> merge(final Collection<Map<K, C>> maps) {
        final Map<K, C> mergedMap = new HashMap<>();

        for (final Map<K, C> map : maps) {
            for (final Map.Entry<K, C> entry : map.entrySet()) {
                mergedMap.merge(entry.getKey(), entry.getValue(), (a, b) -> {
                    a.addAll(b);
                    return a;
                });
            }
        }

        return mergedMap;
    }
}
