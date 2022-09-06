package org.palladiosimulator.somox.discoverer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.palladiosimulator.somox.analyzer.rules.service.Service;

public interface Discoverer extends Service {
    static Stream<String> find(final Path root, final String suffix, final Logger logger) {
        final Path normalizedRoot = Objects.requireNonNull(root)
            .toAbsolutePath()
            .normalize();
        final String normalizedSuffix = Objects.requireNonNull(suffix)
            .toLowerCase()
            .strip();
        try (final Stream<Path> walk = Files.walk(normalizedRoot)) {
            return walk
                .filter(path -> Files.isRegularFile(path) && path.getFileName()
                    .toString()
                    .toLowerCase()
                    .endsWith(normalizedSuffix))
                .map(Path::toAbsolutePath)
                .map(Path::normalize)
                .map(Path::toString);
        } catch (SecurityException | IOException e) {
            logger.error(String.format("No %s files could be found in %s", normalizedSuffix, normalizedRoot), e);
        }
        return Stream.empty();
    }
}
