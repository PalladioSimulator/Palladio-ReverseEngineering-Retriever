package org.palladiosimulator.retriever.extraction.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.palladiosimulator.retriever.extraction.commonalities.CompUnitOrName;

/**
 * The DockerParser parses a docker-compose file to extract a mapping between service names
 * (microservices) and Java model instances. Later, this parser will be replaced with the project
 * in: https://github.com/PalladioSimulator/Palladio-ReverseEngineering-Docker
 */
public class DockerParser {
    private static final String FILE_NAME = "docker-compose";

    private final Path path;
    private final PCMDetector pcmDetector;
    private final Map<String, Set<CompilationUnit>> mapping;

    private static final Logger LOG = Logger.getLogger(DockerParser.class);

    public DockerParser(Path path, PCMDetector pcmDetector) {

        LOG.info("starting docker process");

        this.path = path;
        this.pcmDetector = pcmDetector;
        final InputStream input = getDockerFile();
        final List<String> services = extractServiceNames(input);
        mapping = createServiceComponentMapping(services);
    }

    /**
     * Returns a Stream to the docker-compose file found by walking through a given project
     * directory.
     *
     * @return the docker-compose file as stream
     */
    private InputStream getDockerFile() {

        List<Path> paths = new ArrayList<>();
        try (Stream<Path> files = Files.walk(path)) {
            paths = files.filter(f -> f.getFileName()
                .toString()
                .contains(FILE_NAME))
                .collect(Collectors.toList());
        } catch (final IOException e) {
            e.printStackTrace();
        }
        if (paths.isEmpty()) {
            LOG.info("No docker compose file detected.");
            return null;
        }
        final Path firstPath = paths.get(0);

        final File initialFile = firstPath.toFile();
        try {
            return new FileInputStream(initialFile);
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Extracts the service names within a docker-compose file.
     *
     * @param stream
     *            the docker-compose file
     * @return the list of all service names found in the docker-compose file
     */
    @SuppressWarnings("unchecked")
    private static List<String> extractServiceNames(InputStream stream) {
        // final Yaml yaml = new Yaml();
        final Map<String, Object> object = new HashMap<>(); // (Map<String, Object>)
                                                            // yaml.load(stream);

        // get all service names from the map
        if (!object.containsKey("services")) {
            LOG.info("No property with name 'services' in docker compose file. File not usable");
            return new ArrayList<>();
        }
        return new ArrayList<>(((Map<String, Object>) object.get("services")).keySet());
    }

    /**
     * Creates a mapping between service names and Java model instances to know which component
     * belongs to which microservice
     *
     * @param serviceNames
     *            a list of all service names from a docker-compose file
     * @return the mapping between service names and Java model instances
     */
    private Map<String, Set<CompilationUnit>> createServiceComponentMapping(List<String> serviceNames) {

        final Set<CompUnitOrName> components = pcmDetector.getCompilationUnits();

        final Map<String, Set<CompilationUnit>> serviceToCompMapping = new HashMap<>();

        components.forEach(compUnitOrName -> {
            if (!compUnitOrName.isUnit()) {
                return;
            }
            CompilationUnit comp = compUnitOrName.compilationUnit()
                .get();
            try (Stream<Path> files = Files.walk(path)) {
                // TODO try to find a more robust heuristic
                final List<Path> foundPaths = files.filter(f -> f.toString()
                    .contains(((AbstractTypeDeclaration) comp.types()
                        .get(0)).getName()
                            .getIdentifier()))
                    .collect(Collectors.toList());

                if (!foundPaths.isEmpty()) {
                    serviceNames.forEach(serviceName -> {
                        if (foundPaths.get(0)
                            .toString()
                            .contains(serviceName)) {
                            if (!serviceToCompMapping.containsKey(serviceName)) {
                                serviceToCompMapping.put(serviceName, new HashSet<>());
                            }
                            serviceToCompMapping.get(serviceName)
                                .add(comp);
                        }
                    });
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        });

        return serviceToCompMapping;
    }

    public Map<String, Set<CompilationUnit>> getMapping() {
        return mapping;
    }

}
