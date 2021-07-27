package org.palladiosimulator.somox.analyzer.rules.engine.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.emftext.language.java.containers.impl.CompilationUnitImpl;
import org.junit.jupiter.api.AfterEach;
import org.palladiosimulator.pcm.reliability.FailureType;
import org.palladiosimulator.pcm.repository.DataType;
import org.palladiosimulator.pcm.repository.Interface;
import org.palladiosimulator.pcm.repository.RepositoryComponent;
import org.palladiosimulator.pcm.repository.impl.RepositoryImpl;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;
import org.palladiosimulator.somox.analyzer.rules.engine.ParserAdapter;
import org.palladiosimulator.somox.analyzer.rules.main.RuleEngineAnalyzer;
import org.apache.log4j.Logger;

abstract class RuleEngineTest {
    // Seperate instances for every child test
    private final Logger log = Logger.getLogger(this.getClass());
    
    public static final Path TEST_DIR = Paths.get("res/");
    public static final Path OUT_DIR = TEST_DIR.resolve("out");

    private Set<DefaultRule> rules;
    private RepositoryImpl repo;
    
    private List<RepositoryComponent> components;
    private List<DataType> datatypes;
    private List<FailureType> failuretypes;
    private List<Interface> interfaces;


    /**
     * Tests the basic functionality of the RuleEngineAnalyzer.
     * Requires it to execute without an exception and produce an output file.
     * 
     * @param projectDirectory the name of the project directory that will be analyzed
     */
    protected RuleEngineTest(String projectDirectory, DefaultRule... rules) {
        final Path inPath = TEST_DIR.resolve(projectDirectory);
        final List<CompilationUnitImpl> model = ParserAdapter.generateModelForPath(inPath);
        this.rules = Set.of(rules);
        RuleEngineAnalyzer.executeWith(inPath, OUT_DIR, model, this.rules);

        Path repoPath = Paths.get(OUT_DIR.toString(), "pcm.repository");
        assertTrue(repoPath.toFile().exists());
        
        repo = loadRepository(URI.createFileURI(repoPath.toString()));
        
        components = repo.getComponents__Repository();
        datatypes = repo.getDataTypes__Repository();
        failuretypes = repo.getFailureTypes__Repository();
        interfaces = repo.getInterfaces__Repository();

        log.error("components: " + components.size());
        log.error("datatypes: " + datatypes.size());
        log.error("failuretypes: " + failuretypes.size());
        log.error("interfaces: " + interfaces.size());
    }
    
    abstract void test();
	
	@AfterEach
	void cleanUp() {
        File[] files = OUT_DIR.toFile().listFiles();
        if(files != null) // Some JVMs return null for empty dirs
            for(File f : files) assertTrue(f.delete());
//            for(File f : files) assertTrue(f.renameTo(new File(f.getParentFile(), String.valueOf(System.currentTimeMillis()) + ".repository")));
	}

    public RepositoryImpl getRepo() {
        return repo;
    }

    public Set<DefaultRule> getRules() {
        return Collections.unmodifiableSet(rules);
    }

    public List<RepositoryComponent> getComponents() {
        return Collections.unmodifiableList(components);
    }

    public List<DataType> getDatatypes() {
        return Collections.unmodifiableList(datatypes);
    }

    public List<FailureType> getFailuretypes() {
        return Collections.unmodifiableList(failuretypes);
    }

    public List<Interface> getInterfaces() {
        return Collections.unmodifiableList(interfaces);
    }
    
    private static RepositoryImpl loadRepository(URI repoXMI) {
        final List<EObject> contents = new ResourceSetImpl().getResource(repoXMI, true).getContents();
        
        assertEquals(1, contents.size());
        assertTrue(contents.get(0) instanceof RepositoryImpl);
        
        validate(contents.get(0));
        
        return (RepositoryImpl) contents.get(0);
    }
    
    public static void validate(EObject eObject) 
    {
        EcoreUtil.resolveAll(eObject);
        assertEquals(Diagnostic.OK, Diagnostician.INSTANCE.validate(eObject).getSeverity());
    }
}
