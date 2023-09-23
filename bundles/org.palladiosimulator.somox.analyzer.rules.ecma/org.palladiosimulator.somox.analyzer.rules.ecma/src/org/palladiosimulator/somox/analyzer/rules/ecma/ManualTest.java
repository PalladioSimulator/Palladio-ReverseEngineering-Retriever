package org.palladiosimulator.somox.analyzer.rules.ecma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.openjdk.nashorn.api.scripting.NashornException;
import org.openjdk.nashorn.api.tree.CompilationUnitTree;

public class ManualTest {

    public static String[] findFiles(final String path, final String extension) {
        try (Stream<Path> stream = Files.find(Paths.get(path), Integer.MAX_VALUE,
                (filePath, fileAttributes) -> fileAttributes.isRegularFile())) {
            return stream.filter(filePath -> filePath.getFileName()
                .toString()
                .toLowerCase()
                .endsWith(extension.toLowerCase()))
                .map(Path::toAbsolutePath)
                .map(Path::normalize)
                .sorted()
                .map(String::valueOf)
                .toArray(String[]::new);
        } catch (final IOException e) {
            return new String[0];
        }
    }

    public static void main(String[] args) throws NashornException, IOException {
        final String[] caseStudies = { "piggymetrics-spring.version.2.0.3", "spring-petclinic-microservices-2.3.6" };
        for (final String study : caseStudies) {
            System.out.println(study);
            Map<String, Set<String>> allHttpRequests = new HashMap<>();
            for (final String path : findFiles(
                    "../../../tests/org.palladiosimulator.somox.analyzer.rules.test/res/external/" + study, "js")) {
                final CompilationUnitTree cut = EcmaScript.parse(path);
                allHttpRequests = EcmaScript.join(allHttpRequests, EcmaScript.findAllHttpRequests(cut));
            }
            print(allHttpRequests);
        }

    }

    private static void print(Map<String, Set<String>> map) {
        for (final String key : new TreeSet<>(map.keySet())) {
            System.out.println("\t" + key + " = " + map.get(key));
        }
    }

}
