package org.palladiosimulator.somox.analyzer.rules.main;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.emftext.language.java.containers.ContainersPackage;
import org.emftext.language.java.containers.impl.CompilationUnitImpl;
import org.palladiosimulator.generator.fluent.repository.factory.FluentRepositoryFactory;
import org.palladiosimulator.generator.fluent.shared.util.ModelSaver;
import org.palladiosimulator.generator.fluent.system.api.ISystem;
import org.palladiosimulator.generator.fluent.system.factory.FluentSystemFactory;
import org.palladiosimulator.pcm.core.entity.Entity;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryComponent;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineConfiguration;
import org.palladiosimulator.somox.analyzer.rules.engine.DockerParser;
import org.palladiosimulator.somox.analyzer.rules.engine.IRule;
import org.palladiosimulator.somox.analyzer.rules.engine.PCMDetectorSimple;
import org.palladiosimulator.somox.analyzer.rules.engine.PCMInstanceCreator;
import org.palladiosimulator.somox.analyzer.rules.engine.ParserAdapter;
import org.apache.log4j.Logger;
import org.somox.analyzer.AnalysisResult;
import org.somox.analyzer.ModelAnalyzer;
import static org.somox.analyzer.ModelAnalyzer.Status.FINISHED;
import static org.somox.analyzer.ModelAnalyzer.Status.READY;
import static org.somox.analyzer.ModelAnalyzer.Status.RUNNING;

import org.somox.analyzer.ModelAnalyzerException;
import org.somox.extractor.ExtractionResult;
import org.somox.sourcecodedecorator.SourceCodeDecoratorRepository;

/**
 * The rule engine identifies PCM elements like components and interfaces inside source code via
 * rules specified by a user before. The output of this procedure is a
 * SourceCodeDecoratorRepositoryModel and a PCMRepository model. For this, the engine needs a
 * project directory, an output directory, a JaMoPP model and a IRule file.
 *
 * To use the engine, invoke executeWith(projectPath, outPath, model, rules). To simplify the use,
 * the engine provides the public methods loadRules() and loadModel().
 */
public class RuleEngineAnalyzer implements ModelAnalyzer<RuleEngineConfiguration> {
    private static final Logger LOG = Logger.getLogger(RuleEngineAnalyzer.class);

    private Status status;

    private RuleEngineBlackboard blackboard;

    private static Repository pcm;

    private static SourceCodeDecoratorRepository deco;

    public RuleEngineAnalyzer(RuleEngineBlackboard blackboard) {
        this.blackboard = blackboard;
        init();
    }

    @Override
    public void init() {
        this.status = READY;
    }

    @Override
    public Status getStatus() {
        return this.status;
    }

    /**
     * Returns the current PCM repository model of the engine
     *
     * @return the PCM repository model
     */
    public static Repository getPCMRepository() {
        return pcm;
    }

    /**
     * Returns the current SourceCodeDecoratorRepository model of the engine
     *
     * @return the SourceCodeDecoratorRepository model
     */
    public static SourceCodeDecoratorRepository getDecoratorRepository() {
        return deco;
    }

    @Override
    public AnalysisResult analyze(RuleEngineConfiguration ruleEngineConfiguration,
            HashMap<String, ExtractionResult> extractionResultMap, IProgressMonitor progressMonitor)
            throws ModelAnalyzerException {

        this.status = RUNNING;

        try {
            final URI in = CommonPlugin.asLocalURI(ruleEngineConfiguration.getInputFolder());
            final Path inPath = Paths.get(in.devicePath());

            final URI out = CommonPlugin.asLocalURI(ruleEngineConfiguration.getOutputFolder());
            final Path outPath = Paths.get(out.devicePath());

            final Set<DefaultRule> rules = ruleEngineConfiguration.getSelectedRules();

            final List<CompilationUnitImpl> roots = ParserAdapter.generateModelForPath(inPath, outPath);

            executeWith(inPath, outPath, roots, rules, blackboard);
        } catch (Exception e) {
            throw new ModelAnalyzerException(e.getMessage());
        } finally {
            this.status = FINISHED;
        }

        return this.initializeAnalysisResult();
    }

    /**
     * Tries to find the files for the CompilationUnits in the {@code root} directory. Both takes
     * the CompilationUnits from and saves the associations to the given {@code blackboard}.
     * 
     * @param roots
     *            the directory to search in
     * @param blackboard
     *            the blackboard to save to
     */
    private static void findFilesForCompilationUnits(Path root, RuleEngineBlackboard blackboard) {
        for (CompilationUnitImpl compilationUnit : blackboard.getCompilationUnits()) {
            List<String> pathSegments = new LinkedList<>(compilationUnit.getContainingPackageName());
            pathSegments.add(compilationUnit.getName());
            String guessedPath = String.join(File.separator, pathSegments) + ".java";

            try {
                Files.walk(root)
                    .filter(Files::isRegularFile)
                    .filter(x -> x.endsWith(guessedPath))
                    .forEach(x -> blackboard.addCompilationUnitLocation(compilationUnit, x));
            } catch (IOException e) {
                LOG.warn("An IOException occurred while searching for the files containing the CompilationUnits!");
            }
        }
    }

    /**
     * Extracts PCM elements out of an existing JaMoPP model using an IRule file.
     *
     * @param projectPath
     *            the project directory
     * @param outPath
     *            the output directory
     * @param model
     *            the JaMoPP model
     * @param ruleDoc
     *            the object containing the rules
     */
    public static void executeWith(Path projectPath, Path outPath, List<CompilationUnitImpl> model,
            Set<DefaultRule> rules) {
        executeWith(projectPath, outPath, model, rules, new RuleEngineBlackboard());
    }

    /**
     * Extracts PCM elements out of an existing JaMoPP model using an IRule file.
     *
     * @param projectPath
     *            the project directory
     * @param outPath
     *            the output directory
     * @param model
     *            the JaMoPP model
     * @param ruleDoc
     *            the object containing the rules
     * @param blackboard
     *            the rule engine blackboard
     */
    private static void executeWith(Path projectPath, Path outPath, List<CompilationUnitImpl> model,
            Set<DefaultRule> rules, RuleEngineBlackboard blackboard) {

        // Set up blackboard
        blackboard.setPCMDetector(new PCMDetectorSimple());
        blackboard.addCompilationUnits(model);
        findFilesForCompilationUnits(projectPath, blackboard);

        // Look for build files in projectPath
        Set<Path> buildPaths;
        try {
            buildPaths = Files.walk(projectPath)
                .collect(Collectors.toSet());
        } catch (final IOException e) {
            buildPaths = new HashSet<Path>();
            e.printStackTrace();
        }

        boolean processedLocationless = false;
        // For each unit, execute rules
        for (final CompilationUnitImpl u : model) {
            Set<Path> unitPaths = blackboard.getCompilationUnitLocations(u);
            if (unitPaths.isEmpty()) {
                if (!processedLocationless) {
                    // Execute rules for all CompilationUnits without associated files
                    for (final DefaultRule rule : rules) {
                        rule.getRule(blackboard)
                            .processRules(null);
                    }
                    processedLocationless = true;
                }
                continue;
            }

            // TODO it could happen that a build file is a compilation unit as well.
            // In that case, the build file rule could not assume that all
            // compilation units have been found.

            // It is assumed that files with compilation units cannot be build files
            buildPaths.removeAll(unitPaths);

            for (final DefaultRule rule : rules) {
                for (final Path path : unitPaths) {
                    rule.getRule(blackboard)
                        .processRules(path);
                }
            }
        }
        LOG.info("Applied rules to the compilation units");

        // For each potential build file, execute rules
        for (final Path path : buildPaths) {
            for (final DefaultRule rule : rules) {
                rule.getRule(blackboard)
                    .processRules(path);
            }
        }
        LOG.info("Applied rules to the build files");

        // Parses the docker-compose file to get a mapping between microservice names and components
        // for creating composite components for each microservice
        final DockerParser dockerParser = new DockerParser(projectPath, blackboard.getPCMDetector());
        final Map<String, List<CompilationUnitImpl>> mapping = dockerParser.getMapping();

        // Creates a PCM repository with systems, components, interfaces and roles
        pcm = new PCMInstanceCreator(blackboard).createPCM(mapping);

        // Create the build file systems
        Map<RepositoryComponent, CompilationUnitImpl> repoCompLocations = blackboard.getRepositoryComponentLocations();
        Map<CompilationUnitImpl, RepositoryComponent> invertedEntityLocations = new HashMap<>();
        for (Entry<RepositoryComponent, CompilationUnitImpl> entry : repoCompLocations.entrySet()) {
            invertedEntityLocations.put(entry.getValue(), entry.getKey());
        }

        FluentSystemFactory create = new FluentSystemFactory();
        for (Entry<Path, Set<CompilationUnitImpl>> entry : blackboard.getSystemAssociations()
            .entrySet()) {
            // TODO better name
            ISystem system = create.newSystem()
                .withName(entry.getKey()
                    .toString());
            boolean hasChildren = false;
            for (CompilationUnitImpl compUnit : entry.getValue()) {
                RepositoryComponent repoComp = invertedEntityLocations.get(compUnit);
                // Only compilation units that have been processed by some other rule can be
                // added to a system
                if (repoComp != null) {
                    system.addToSystem(create.newAssemblyContext()
                        .withEncapsulatedComponent(repoComp));
                    hasChildren = true;
                }
            }
            // Only save systems that contain something to the repository
            if (hasChildren) {
                blackboard.putSystemPath(system.createSystemNow(), entry.getKey());
            }
        }

        // Persist the repository at ./pcm.repository
        ModelSaver.saveRepository(pcm, outPath.resolve("pcm")
            .toString(), false);
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
                final IRule rules = (IRule) instance;
                return rules;
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Loads an external JaMoPP model.
     *
     * @return the JaMoPP model instances for each java file
     */
    public static List<CompilationUnitImpl> loadModel(URI model) {
        final ResourceSet rs = new ResourceSetImpl();
        rs.getPackageRegistry()
            .put(ContainersPackage.eNS_URI, ContainersPackage.eINSTANCE);
        rs.getResourceFactoryRegistry()
            .getExtensionToFactoryMap()
            .put("containers", new XMIResourceFactoryImpl());

        final Resource res = rs.createResource(model);
        try {
            res.load(null);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        final List<EObject> contents = res.getContents();
        return contents.stream()
            .map(content -> (CompilationUnitImpl) content)
            .filter(compi -> compi.getName() != null)
            .collect(Collectors.toList());
    }
}
