package org.palladiosimulator.somox.analyzer.rules.main;

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

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.palladiosimulator.generator.fluent.shared.util.ModelSaver;
import org.palladiosimulator.generator.fluent.system.api.ISystem;
import org.palladiosimulator.generator.fluent.system.factory.FluentSystemFactory;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryComponent;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineConfiguration;
import org.palladiosimulator.somox.analyzer.rules.engine.DockerParser;
import org.palladiosimulator.somox.analyzer.rules.engine.IRule;
import org.palladiosimulator.somox.analyzer.rules.engine.PCMDetector;
import org.palladiosimulator.somox.analyzer.rules.engine.PCMInstanceCreator;

/**
 * The rule engine identifies PCM elements like components and interfaces inside source code via
 * rules specified by a user before. The output of this procedure is a
 * SourceCodeDecoratorRepositoryModel and a PCMRepository model. For this, the engine needs a
 * project directory, an output directory, a Java model and a IRule file.
 *
 * To use the engine, invoke executeWith(projectPath, outPath, model, rules). To simplify the use,
 * the engine provides the public methods loadRules() and loadModel().
 */
public class RuleEngineAnalyzer {
    private static final Logger LOG = Logger.getLogger(RuleEngineAnalyzer.class);

    private final RuleEngineBlackboard blackboard;

    private static Repository pcm;

    public RuleEngineAnalyzer(RuleEngineBlackboard blackboard) {
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

    public void analyze(RuleEngineConfiguration ruleEngineConfiguration, IProgressMonitor progressMonitor)
            throws RuleEngineException {

        try {
            final URI in = CommonPlugin.asLocalURI(ruleEngineConfiguration.getInputFolder());
            final Path inPath = Paths.get(in.devicePath());

            final URI out = CommonPlugin.asLocalURI(ruleEngineConfiguration.getOutputFolder());
            final Path outPath = Paths.get(out.devicePath());

            final Set<DefaultRule> rules = ruleEngineConfiguration.getSelectedRules();

            executeWith(inPath, outPath, rules, blackboard);
        } catch (Exception e) {
            throw new RuleEngineException("Analysis did not complete successfully", e);
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
    public static void executeWith(Path projectPath, Path outPath, List<CompilationUnit> model,
            Set<DefaultRule> rules) {
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
     *            the rule engine blackboard, containing (among other things) the discovered files
     */
    private static void executeWith(Path projectPath, Path outPath, Set<DefaultRule> rules,
            RuleEngineBlackboard blackboard) {

        // Set up blackboard
        blackboard.setPCMDetector(new PCMDetector());

        Set<Path> discoveredFiles = blackboard.getDiscoveredPaths();

        // For each discovered file, execute non-build rules
        for (final Path discoveredFile : discoveredFiles) {
            for (final DefaultRule rule : rules) {
                if (!rule.isBuildRule()) {
                    rule.getRule(blackboard)
                        .processRules(discoveredFile);
                }
            }
        }
        LOG.info("Applied non-build rules");

        // Execute rules for build files (on all files, the rules filter themselves).
        for (final Path discoveredFile : discoveredFiles) {
            for (final DefaultRule rule : rules) {
                if (rule.isBuildRule()) {
                    rule.getRule(blackboard)
                        .processRules(discoveredFile);
                }
            }
        }
        LOG.info("Applied build rules");

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
        blackboard.addPartition(RuleEngineConfiguration.RULE_ENGINE_BLACKBOARD_KEY_REPOSITORY, pcm);
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
    public static IRule loadRules(String namespace, Path rulesFile) {

        final File file = rulesFile.toFile();

        try (URLClassLoader loader = new URLClassLoader(new URL[] { file.toURI()
            .toURL() })) {
            final Class<?> c = loader.loadClass(namespace + file.getName()
                .replace(".class", ""));
            final Object instance = c.getDeclaredConstructor()
                .newInstance();
            if (instance instanceof IRule) {
                return (IRule) instance;
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
