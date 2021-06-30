package org.palladiosimulator.somox.analyzer.rules.engine.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.emftext.language.java.containers.impl.CompilationUnitImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.palladiosimulator.pcm.reliability.FailureType;
import org.palladiosimulator.pcm.repository.DataType;
import org.palladiosimulator.pcm.repository.Interface;
import org.palladiosimulator.pcm.repository.RepositoryComponent;
import org.palladiosimulator.pcm.repository.impl.RepositoryImpl;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;
import org.palladiosimulator.somox.analyzer.rules.engine.ParserAdapter;
import org.palladiosimulator.somox.analyzer.rules.main.RuleEngineAnalyzer;

class RuleEngineTest {
    
    private static final String TEST_DIR = "res/";
    private static final Path outPath = Paths.get(TEST_DIR, "out");

    /**
     * Tests the basic functionality of the RuleEngineAnalyzer.
     * Requires it to execute without an exception and produce an output file.
     */
	@Test
	void testBasicFunctionality() {
	    final Path inPath = Paths.get(TEST_DIR, "BasicProject");
        final List<CompilationUnitImpl> model = ParserAdapter.generateModelForPath(inPath);
        final Set<DefaultRule> rules = new HashSet<>();
     	RuleEngineAnalyzer.executeWith(inPath, outPath, model, rules);
		
		assertTrue(Paths.get(outPath.toString(), "pcm.repository").toFile().exists());
	}

	@Test
	@Disabled("Due to static repository in RuleEngineAnalyzer")
	void testSpringRule() {
        final Path inPath = Paths.get(TEST_DIR, "SpringProject");
        final List<CompilationUnitImpl> model = ParserAdapter.generateModelForPath(inPath);
        final Set<DefaultRule> rules = new HashSet<>();
        rules.add(DefaultRule.SPRING);
        RuleEngineAnalyzer.executeWith(inPath, outPath, model, rules);

        Path repoPath = Paths.get(outPath.toString(), "pcm.repository");
        assertTrue(repoPath.toFile().exists());
        
        RepositoryImpl repo = loadRepository(URI.createFileURI(repoPath.toString()));
        
        List<RepositoryComponent> components = repo.getComponents__Repository();
        List<DataType> datatypes = repo.getDataTypes__Repository();
        List<FailureType> failuretypes = repo.getFailureTypes__Repository();
        List<Interface> interfaces = repo.getInterfaces__Repository();

        assertEquals(1, components.size());
        assertEquals(1, datatypes.size());
        assertEquals(0, failuretypes.size());
        assertEquals(0, interfaces.size());
        
        assertEquals("spring_AComponent",  components.get(0).getEntityName());
	}

    @Test
    @Disabled("Due to a potential bug in PCMInstanceCreator")
    void testJaxRsRule() {
        final Path inPath = Paths.get(TEST_DIR, "JaxRsProject");
        final List<CompilationUnitImpl> model = ParserAdapter.generateModelForPath(inPath);
        final Set<DefaultRule> rules = new HashSet<>();
        rules.add(DefaultRule.JAX_RS);
        RuleEngineAnalyzer.executeWith(inPath, outPath, model, rules);

        Path repoPath = Paths.get(outPath.toString(), "pcm.repository");
        assertTrue(repoPath.toFile().exists());
        
        RepositoryImpl repo = loadRepository(URI.createFileURI(repoPath.toString()));
        
        List<RepositoryComponent> components = repo.getComponents__Repository();
        List<DataType> datatypes = repo.getDataTypes__Repository();
        List<FailureType> failuretypes = repo.getFailureTypes__Repository();
        List<Interface> interfaces = repo.getInterfaces__Repository();

        assertEquals(3, components.size());
        assertEquals(1, datatypes.size());
        assertEquals(0, failuretypes.size());
        assertEquals(2, interfaces.size());
        
        assertEquals("spring_AComponent",  components.get(0).getEntityName());
    }
	
	@AfterAll
	static void cleanUp() {
	    outPath.toFile().delete();
	}
	
	private static RepositoryImpl loadRepository(URI repoXMI) {
        final List<EObject> contents = new ResourceSetImpl().getResource(repoXMI, true).getContents();
        
        assertEquals(1, contents.size());
        assertTrue(contents.get(0) instanceof RepositoryImpl);
        
        validate(contents.get(0));
        
        return (RepositoryImpl) contents.get(0);
	}
	
	private static void validate(EObject eObject) 
	{
	    EcoreUtil.resolveAll(eObject);
	    assertEquals(Diagnostician.INSTANCE.validate(eObject).getSeverity(), Diagnostic.OK);
	}
}
