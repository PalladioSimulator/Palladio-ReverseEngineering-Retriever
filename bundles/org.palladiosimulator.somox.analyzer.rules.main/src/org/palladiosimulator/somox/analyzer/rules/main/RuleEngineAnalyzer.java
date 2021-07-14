package org.palladiosimulator.somox.analyzer.rules.main;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineConfiguration;
import org.palladiosimulator.somox.analyzer.rules.engine.DockerParser;
import org.palladiosimulator.somox.analyzer.rules.engine.IRule;
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
* The rule engine identifies PCM elements like components and interfaces inside source code via rules specified by a user before.
* The output of this procedure is a SourceCodeDecoratorRepositoryModel and a PCMRepository model.
* For this, the engine needs a project directory, an output directory, a JaMoPP model and a IRule file.
*
* To use the engine, invoke executeWith(projectPath, outPath, model, rules).
* To simplify the use, the engine provides the public methods loadRules() and loadModel().
*/
public class RuleEngineAnalyzer implements ModelAnalyzer<RuleEngineConfiguration> {
    private static final Logger LOG = Logger.getLogger(RuleEngineAnalyzer.class);

    private Status status;

    private static Repository pcm;

    private static SourceCodeDecoratorRepository deco;

    public RuleEngineAnalyzer() {
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

            final List<CompilationUnitImpl> roots = ParserAdapter.generateModelForPath(inPath);

            executeWith(inPath, outPath, roots, rules);
        } catch (Exception e) {
            throw new ModelAnalyzerException(e.getMessage());
        } finally {
            this.status = FINISHED;
        }

        return this.initializeAnalysisResult();
    }

    /**
    * Extracts PCM elements out of an existing JaMoPP model using an IRule file.
    *
    * @param  projectPath 	the project directory
    * @param  outPath       the output directory
    * @param  model 		the JaMoPP model
    * @param  ruleDoc 		the object containing the rules
    */
    public static void executeWith(Path projectPath, Path outPath, List<CompilationUnitImpl> model, Set<DefaultRule> rules) {

        // For each unit, execute rules
        for (final CompilationUnitImpl u : model) {
            for (final DefaultRule rule : rules) {
                rule.getRule().processRules(u);
            }
        }
        LOG.info("Applied rules to the compilation units");

        // Parses the docker-compose file to get a mapping between microservice names and components
        // for creating composite components for each microservice
        final DockerParser dockerParser = new DockerParser(projectPath);
        final Map<String, List<CompilationUnitImpl>> mapping = dockerParser.getMapping();
        
        // Creates a PCM repository with components, interfaces and roles
        pcm = new PCMInstanceCreator().createPCM(mapping);

        // Persist the repository at ./pcm.repository
        PCMInstanceCreator.saveRepository(pcm, outPath, "pcm.repository", true);
    }

    /**
    * Loads an external rules class file. For that the full qualified name of the xtend class has to be known
    *
    * @param  	namespace the string containing the namespace of the class implementing the IRule Interface
    * @param    rules the path to a .class file containing the rules
    * @return	the rules from the specified (via gui) file system place
    */
    public static IRule loadRules(String namespace, Path rulesFile) {

        final File file = rulesFile.toFile();

        try (URLClassLoader loader = new URLClassLoader(new URL[] { file.toURI().toURL() })) {
            final Class<?> c = loader.loadClass(namespace + file.getName().replace(".class", ""));
            final Object instance = c.getDeclaredConstructor().newInstance();
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
        rs.getPackageRegistry().put(ContainersPackage.eNS_URI, ContainersPackage.eINSTANCE);
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("containers", new XMIResourceFactoryImpl());

        final Resource res = rs.createResource(model);
        try {
            res.load(null);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        final List<EObject> contents = res.getContents();
        return contents.stream().map(content -> (CompilationUnitImpl) content).filter(compi -> compi.getName() != null)
                .collect(Collectors.toList());
    }
}
