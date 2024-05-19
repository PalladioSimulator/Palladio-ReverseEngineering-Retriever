package org.palladiosimulator.retriever.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

public interface Discoverer extends Service {
    static Stream<Path> find(final Path root, final String suffix, final Logger logger) {
        final Path normalizedRoot = Objects.requireNonNull(root)
            .toAbsolutePath()
            .normalize();
        final String normalizedSuffix = Objects.requireNonNull(suffix)
            .toLowerCase()
            .strip();
        try (final Stream<Path> walk = Files.walk(normalizedRoot)) {
            return walk.filter(path -> Files.isRegularFile(path) && path.getFileName()
                .toString()
                .toLowerCase()
                .endsWith(normalizedSuffix))
                .map(Path::toAbsolutePath)
                .map(Path::normalize)
                .distinct()
                .collect(Collectors.toSet())
                .stream();
        } catch (SecurityException | IOException e) {
            logger.error(String.format("No %s files could be found in %s", normalizedSuffix, normalizedRoot), e);
        }
        return Stream.empty();
    }

    @Override
    default Set<String> getRequiredServices() {
        return Set.of();
    }

    @Override
    default Set<String> getDependentServices() {
        return Set.of();
    }
}
