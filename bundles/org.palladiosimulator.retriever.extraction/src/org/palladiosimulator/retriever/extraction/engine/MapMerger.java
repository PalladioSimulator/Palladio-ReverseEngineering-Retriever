package org.palladiosimulator.retriever.extraction.engine;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MapMerger {

    private MapMerger() {
        throw new IllegalStateException();
    }

    public static <K, V> Map<K, List<V>> merge(final Collection<Map<K, List<V>>> maps) {
        final Map<K, List<V>> mergedMap = new HashMap<>();

        for (final Map<K, List<V>> map : maps) {
            for (final Map.Entry<K, List<V>> entry : map.entrySet()) {
                mergedMap.merge(entry.getKey(), entry.getValue(), (a, b) -> Stream.concat(a.stream(), b.stream())
                    .collect(Collectors.toList()));
            }
        }

        return mergedMap;
    }
}
