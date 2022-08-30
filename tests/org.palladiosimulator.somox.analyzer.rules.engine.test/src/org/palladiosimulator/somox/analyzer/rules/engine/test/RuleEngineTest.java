package org.palladiosimulator.somox.analyzer.rules.engine.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.junit.jupiter.api.AfterEach;
import org.palladiosimulator.pcm.reliability.FailureType;
import org.palladiosimulator.pcm.repository.DataType;
import org.palladiosimulator.pcm.repository.Interface;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.RepositoryComponent;
import org.palladiosimulator.pcm.repository.impl.RepositoryImpl;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineConfiguration;
import org.palladiosimulator.somox.analyzer.rules.main.RuleEngineAnalyzer;
import org.palladiosimulator.somox.analyzer.rules.main.RuleEngineException;
import org.palladiosimulator.somox.discoverer.Discoverer;
import org.palladiosimulator.somox.discoverer.EmfTextDiscoverer;
import org.palladiosimulator.somox.discoverer.JavaDiscoverer;

import com.google.common.collect.Sets;

import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

import org.apache.log4j.Logger;

abstract class RuleEngineTest {
    // Seperate instances for every child test
    private final Logger log = Logger.getLogger(this.getClass());

    public static final URI TEST_DIR = CommonPlugin
        .asLocalURI(URI.createFileURI(URI.decode(new File("res").getAbsolutePath())));
    public static final URI OUT_DIR = TEST_DIR.appendSegment("out");

    private RuleEngineConfiguration jdtConfig = new RuleEngineConfiguration();
    private RuleEngineConfiguration emfTextConfig = new RuleEngineConfiguration();
    private boolean isJDTCreated;
    private boolean isEMFTextCreated;
    private Set<DefaultRule> rules;
    private RepositoryImpl jdtRepo;
    private RepositoryImpl emfTextRepo;

    private List<RepositoryComponent> jdtComponents;
    private List<DataType> jdtDatatypes;
    private List<FailureType> jdtFailuretypes;
    private List<Interface> jdtInterfaces;
    
    private List<RepositoryComponent> emfTextComponents;
    private List<DataType> emfTextDatatypes;
    private List<FailureType> emfTextFailuretypes;
    private List<Interface> emfTextInterfaces;

    /**
     * Tests the basic functionality of the RuleEngineAnalyzer. Requires it to execute without an
     * exception and produce an output file.
     * 
     * @param projectDirectory
     *            the name of the project directory that will be analyzed
     */
    protected RuleEngineTest(String projectDirectory, DefaultRule... rules) {
        RuleEngineBlackboard jdtBlackboard = new RuleEngineBlackboard();
        RuleEngineAnalyzer jdtAnalyzer = new RuleEngineAnalyzer(jdtBlackboard);
        Discoverer jdtDiscoverer = new JavaDiscoverer();

        RuleEngineBlackboard emfTextBlackboard = new RuleEngineBlackboard();
        RuleEngineAnalyzer emfTextAnalyzer = new RuleEngineAnalyzer(emfTextBlackboard);
        Discoverer emfTextDiscoverer = new EmfTextDiscoverer();

        this.rules = Set.of(rules);

        jdtConfig.setInputFolder(TEST_DIR.appendSegments(projectDirectory.split("/")));
        jdtConfig.setOutputFolder(OUT_DIR.appendSegment("jdt"));
        jdtConfig.setUseEMFTextParser(false);
        jdtConfig.setSelectedRules(this.rules);

        emfTextConfig.setInputFolder(TEST_DIR.appendSegments(projectDirectory.split("/")));
        emfTextConfig.setOutputFolder(OUT_DIR.appendSegment("emfText"));
        emfTextConfig.setUseEMFTextParser(true);
        emfTextConfig.setSelectedRules(this.rules);
        
        try {
            jdtDiscoverer.create(jdtConfig, jdtBlackboard)
                .execute(null);
            jdtAnalyzer.analyze(jdtConfig, null);
            isJDTCreated = true;
        } catch (RuleEngineException | JobFailedException | UserCanceledException e) {
        	isJDTCreated = false;
        }

        try {
            emfTextDiscoverer.create(emfTextConfig, emfTextBlackboard)
                .execute(null);
            emfTextAnalyzer.analyze(emfTextConfig, null);
            isEMFTextCreated = true;
        } catch (RuleEngineException | JobFailedException | UserCanceledException e) {
        	isEMFTextCreated = false;
        }

        String jdtRepoPath = OUT_DIR.appendSegment("jdt").appendSegment("pcm.repository")
            .devicePath();
        assertTrue(!isJDTCreated || new File(jdtRepoPath).exists());

        String emfTextRepoPath = OUT_DIR.appendSegment("emfText").appendSegment("pcm.repository")
            .devicePath();
        assertTrue(!isEMFTextCreated || new File(emfTextRepoPath).exists());

        if (isJDTCreated) {
        	jdtRepo = loadRepository(URI.createFileURI(jdtRepoPath));
        }
        if (isEMFTextCreated) {
        	emfTextRepo = loadRepository(URI.createFileURI(emfTextRepoPath));
        }

        if (isJDTCreated) {
	        jdtComponents = jdtRepo.getComponents__Repository();
	        jdtDatatypes = jdtRepo.getDataTypes__Repository();
	        jdtFailuretypes = jdtRepo.getFailureTypes__Repository();
	        jdtInterfaces = jdtRepo.getInterfaces__Repository();
        }

        if (isEMFTextCreated) {
	        emfTextComponents = emfTextRepo.getComponents__Repository();
	        emfTextDatatypes = emfTextRepo.getDataTypes__Repository();
	        emfTextFailuretypes = emfTextRepo.getFailureTypes__Repository();
	        emfTextInterfaces = emfTextRepo.getInterfaces__Repository();
        }
    }

    abstract void test(boolean emfText);

    @AfterEach
    void cleanUp() {
        File target = new File(OUT_DIR.devicePath(), this.getClass()
            .getSimpleName() + ".repository");
        target.delete();
        if (!new File(OUT_DIR.devicePath(), "pcm.repository").renameTo(target)) {
            log.error("Could not save created repository to \"" + target.getAbsolutePath() + "\"!");
        }
    }

    public RuleEngineConfiguration getConfig(boolean emfText) {
    	assertCreated(emfText);
    	if (emfText) {
    		return emfTextConfig;
    	} else {
    		return jdtConfig;
    	}
    }

    public RepositoryImpl getRepo(boolean emfText) {
    	assertCreated(emfText);
    	if (emfText) {
    		return emfTextRepo;
    	} else {
    		return jdtRepo;
    	}
    }

    public Set<DefaultRule> getRules() {
        return Collections.unmodifiableSet(rules);
    }

    public List<RepositoryComponent> getComponents(boolean emfText) {
    	assertCreated(emfText);
    	if (emfText) {
    		return Collections.unmodifiableList(emfTextComponents);
    	} else {
            return Collections.unmodifiableList(jdtComponents);
    	}
    }

    public List<DataType> getDatatypes(boolean emfText) {
    	assertCreated(emfText);
    	if (emfText) {
    		return Collections.unmodifiableList(emfTextDatatypes);
    	} else {
            return Collections.unmodifiableList(jdtDatatypes);
    	}
    }

    public List<FailureType> getFailuretypes(boolean emfText) {
    	assertCreated(emfText);
    	if (emfText) {
    		return Collections.unmodifiableList(emfTextFailuretypes);
    	} else {
            return Collections.unmodifiableList(jdtFailuretypes);
    	}
    }

    public List<Interface> getInterfaces(boolean emfText) {
    	assertCreated(emfText);
    	if (emfText) {
    		return Collections.unmodifiableList(emfTextInterfaces);
    	} else {
            return Collections.unmodifiableList(jdtInterfaces);
    	}
    }

    public boolean containsComponent(String name, boolean emfText) {
        return getComponents(emfText).stream()
            .anyMatch(x -> x.getEntityName()
                .equals(name));
    }

    public boolean containsOperationInterface(String name, boolean emfText) {
        return getInterfaces(emfText).stream()
            .filter(OperationInterface.class::isInstance)
            .anyMatch(x -> x.getEntityName()
                .equals(name));
    }

    public boolean containsOperationSignature(String interfaceName, String signatureName, boolean emfText) {
        return !getOperationSignature(interfaceName, signatureName, emfText).isEmpty();
    }

    public int getSignatureMaxParameterCount(String interfaceName, String signatureName, boolean emfText) {
        Set<OperationSignature> sigs = getOperationSignature(interfaceName, signatureName, emfText);
        return sigs.stream()
            .map(OperationSignature::getParameters__OperationSignature)
            .map(List::size)
            .reduce(0, Math::max);
    }

    public void assertMaxParameterCount(int expectedMaxParameterCount, String interfaceName, String signatureName, boolean emfText) {
        assertTrue(containsOperationInterface(interfaceName, emfText));
        assertTrue(containsOperationSignature(interfaceName, signatureName, emfText));
        assertEquals(expectedMaxParameterCount, getSignatureMaxParameterCount(interfaceName, signatureName, emfText));
    }

    private Set<OperationSignature> getOperationSignature(String interfaceName, String signatureName, boolean emfText) {
        Set<OperationSignature> sigs = getInterfaces(emfText).stream()
            .filter(OperationInterface.class::isInstance)
            .map(OperationInterface.class::cast)
            .filter(x -> x.getEntityName()
                .equals(interfaceName))
            .map(x -> x.getSignatures__OperationInterface()
                .stream()
                .filter(y -> y.getEntityName()
                    .equals(signatureName))
                .collect(Collectors.toSet()))
            .collect(Collectors.reducing(new HashSet<OperationSignature>(), Sets::union));
        return sigs;
    }
    
    private void assertCreated(boolean emfText) {
    	if (emfText) {
    		assertTrue(isEMFTextCreated, "Failed to create model using EMFTextDiscoverer!");
    	} else {
    		assertTrue(isJDTCreated, "Failed to create model using JavaDiscoverer!");
    	}
    }

    public static RepositoryImpl loadRepository(URI repoXMI) {
        final List<EObject> contents = new ResourceSetImpl().getResource(repoXMI, true)
            .getContents();

        assertEquals(1, contents.size());
        assertTrue(contents.get(0) instanceof RepositoryImpl);

        // TODO activate this again when SEFF is included
        // validate(contents.get(0));

        return (RepositoryImpl) contents.get(0);
    }

    public static void validate(EObject eObject) {
        EcoreUtil.resolveAll(eObject);
        assertEquals(Diagnostic.OK, Diagnostician.INSTANCE.validate(eObject)
            .getSeverity());
    }
}
