package org.palladiosimulator.retriever.extraction.discoverers.wrappers;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class YamlMapper implements Function<String, Optional<Object>> {
    private final Iterable<Object> subfiles;

    public YamlMapper(final Iterable<Object> content) {
        this.subfiles = content;
    }

    @Override
    public Optional<Object> apply(final String fullKey) {
        final String[] segments = fullKey.split("\\.");

        for (final Object subfile : this.subfiles) {

            boolean failed = false;
            Object currentNode = subfile;
            for (final String segment : segments) {
                final Optional<Object> nextNode = this.load(segment, currentNode);
                if (nextNode.isEmpty()) {
                    failed = true;
                    break;
                }
                currentNode = nextNode.get();
            }

            if (!failed) {
                return Optional.of(currentNode);
            }
        }

        return Optional.empty();
    }

    private Optional<Object> load(final String key, final Object yamlObject) {
        if (yamlObject instanceof final Map map) {
            final Object value = map.get(key);
            return Optional.ofNullable(value);
        }
        return Optional.empty();
    }
}