package org.palladiosimulator.retriever.core.main;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.palladiosimulator.generator.fluent.shared.util.ModelSaver;
import org.palladiosimulator.generator.fluent.system.api.ISystem;
import org.palladiosimulator.generator.fluent.system.factory.FluentSystemFactory;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryComponent;
import org.palladiosimulator.retriever.extraction.blackboard.RetrieverBlackboard;
import org.palladiosimulator.retriever.extraction.engine.DockerParser;
import org.palladiosimulator.retriever.extraction.engine.PCMInstanceCreator;
import org.palladiosimulator.retriever.extraction.engine.Rule;
import org.palladiosimulator.retriever.extraction.engine.RetrieverConfiguration;

/**
 * Retriever identifies PCM elements like components and interfaces inside source code via rules
 * specified by a user before. The output of this procedure is a SourceCodeDecoratorRepositoryModel
 * and a PCMRepository model. For this, the engine needs a project directory, an output directory, a
 * Java model and a IRule file.
 *
 * To use the engine, invoke executeWith(projectPath, outPath, model, rules). To simplify the use,
 * the engine provides the public methods loadRules() and loadModel().
 */
public class Retriever {
    private final RetrieverBlackboard blackboard;

    private static Repository pcm;

    public Retriever(RetrieverBlackboard blackboard) {
        this.blackboard = blackboard;
    }

    /**
     * Returns the current PCM repository model of the engine
     *
     * @return the PCM repository model
     */
    public static Repository getPCMRepository() {
        return pcm;
    }

    public void analyze(RetrieverConfiguration configuration, IProgressMonitor progressMonitor)
            throws RetrieverException {

        try {
            final URI in = CommonPlugin.asLocalURI(configuration.getInputFolder());
            final Path inPath = Paths.get(in.devicePath());

            final URI out = CommonPlugin.asLocalURI(configuration.getOutputFolder());
            final Path outPath = Paths.get(out.devicePath());

            final Set<Rule> rules = configuration.getConfig(Rule.class)
                .getSelected();

            executeWith(inPath, outPath, rules, blackboard);
        } catch (Exception e) {
            throw new RetrieverException("Analysis did not complete successfully", e);
        }
    }

    /**
     * Extracts PCM elements out of an existing Eclipse JDT model using an IRule file.
     *
     * @param projectPath
     *            the project directory
     * @param outPath
     *            the output directory
     * @param model
     *            the Java model
     * @param ruleDoc
     *            the object containing the rules
     */
    public static void executeWith(Path projectPath, Path outPath, List<CompilationUnit> model, Set<Rule> rules) {
        executeWith(projectPath, outPath, model, rules);
    }

    /**
     * Extracts PCM elements out of discovered files using rules.
     *
     * @param projectPath
     *            the project directory
     * @param outPath
     *            the output directory
     * @param rules
     *            the rules
     * @param blackboard
     *            the Retriever blackboard, containing (among other things) the discovered files
     */
    private static void executeWith(Path projectPath, Path outPath, Set<Rule> rules, RetrieverBlackboard blackboard) {
        // Creates a PCM repository with systems, components, interfaces and roles

        // Parses the docker-compose file to get a mapping between microservice names and
        // components for creating composite components for each microservice
        final DockerParser dockerParser = new DockerParser(projectPath, blackboard.getPCMDetector());
        final Map<String, Set<CompilationUnit>> mapping = dockerParser.getMapping();

        pcm = new PCMInstanceCreator(blackboard).createPCM(mapping);

        // Create the build file systems
        Map<RepositoryComponent, CompilationUnit> repoCompLocations = blackboard.getRepositoryComponentLocations();
        Map<CompilationUnit, RepositoryComponent> invertedEntityLocations = new HashMap<>();
        for (Entry<RepositoryComponent, CompilationUnit> entry : repoCompLocations.entrySet()) {
            invertedEntityLocations.put(entry.getValue(), entry.getKey());
        }

        FluentSystemFactory create = new FluentSystemFactory();
        for (Entry<Path, Set<CompilationUnit>> entry : blackboard.getSystemAssociations()
            .entrySet()) {
            // TODO better name
            ISystem system = create.newSystem()
                .withName(entry.getKey()
                    .toString());
            boolean hasChildren = false;
            for (CompilationUnit compUnit : entry.getValue()) {
                RepositoryComponent repoComp = invertedEntityLocations.get(compUnit);
                // Only compilation units that have been processed by some other rule can be
                // added to a system
                if (repoComp != null) {
                    system.addToSystem(create.newAssemblyContext()
                        .withEncapsulatedComponent(repoComp)
                        .withName(repoComp.getEntityName()));
                    hasChildren = true;
                }
            }
            // Only save systems that contain something to the repository
            if (hasChildren) {
                blackboard.putSystemPath(system.createSystemNow(), entry.getKey());
            }
        }

        // Persist the repository at ./pcm.repository
        blackboard.addPartition(RetrieverBlackboard.KEY_REPOSITORY, pcm);
        ModelSaver.saveRepository(pcm, outPath.toString(), "pcm");
    }

    /**
     * Loads an external rules class file. For that the full qualified name of the xtend class has
     * to be known
     *
     * @param namespace
     *            the string containing the namespace of the class implementing the IRule Interface
     * @param rules
     *            the path to a .class file containing the rules
     * @return the rules from the specified (via gui) file system place
     */
    public static Rule loadRules(String namespace, Path rulesFile) {

        final File file = rulesFile.toFile();

        try (URLClassLoader loader = new URLClassLoader(new URL[] { file.toURI()
            .toURL() })) {
            final Class<?> c = loader.loadClass(namespace + file.getName()
                .replace(".class", ""));
            final Object instance = c.getDeclaredConstructor()
                .newInstance();
            if (instance instanceof Rule) {
                return (Rule) instance;
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
