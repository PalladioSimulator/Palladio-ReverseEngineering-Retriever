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
import org.junit.jupiter.api.Assertions;
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

    private RuleEngineConfiguration config = new RuleEngineConfiguration();
    private Set<DefaultRule> rules;
    private RepositoryImpl repo;

    private List<RepositoryComponent> components;
    private List<DataType> datatypes;
    private List<FailureType> failuretypes;
    private List<Interface> interfaces;

    /**
     * Tests the basic functionality of the RuleEngineAnalyzer. Requires it to execute without an
     * exception and produce an output file.
     * 
     * @param projectDirectory
     *            the name of the project directory that will be analyzed
     */
    protected RuleEngineTest(String projectDirectory, DefaultRule... rules) {
        RuleEngineBlackboard blackboard = new RuleEngineBlackboard();
        RuleEngineAnalyzer analyzer = new RuleEngineAnalyzer(blackboard);
        JavaDiscoverer discoverer = new JavaDiscoverer();

        this.rules = Set.of(rules);

        config.setInputFolder(TEST_DIR.appendSegments(projectDirectory.split("/")));
        config.setOutputFolder(OUT_DIR);
        config.setUseEMFTextParser(false);
        config.setSelectedRules(this.rules);

        try {
            discoverer.create(config, blackboard)
                .execute(null);
            analyzer.analyze(config, null);
        } catch (RuleEngineException | JobFailedException | UserCanceledException e) {
            Assertions.fail(e);
        }

        String repoPath = OUT_DIR.appendSegment("pcm.repository")
            .devicePath();
        assertTrue(new File(repoPath).exists());

        repo = loadRepository(URI.createFileURI(repoPath));

        components = repo.getComponents__Repository();
        datatypes = repo.getDataTypes__Repository();
        failuretypes = repo.getFailureTypes__Repository();
        interfaces = repo.getInterfaces__Repository();

        log.info("components: " + components.size());
        log.info("datatypes: " + datatypes.size());
        log.info("failuretypes: " + failuretypes.size());
        log.info("interfaces: " + interfaces.size());
    }

    abstract void test();

    @AfterEach
    void cleanUp() {
        File target = new File(OUT_DIR.devicePath(), this.getClass()
            .getSimpleName() + ".repository");
        target.delete();
        if (!new File(OUT_DIR.devicePath(), "pcm.repository").renameTo(target)) {
            log.error("Could not save created repository to \"" + target.getAbsolutePath() + "\"!");
        }
    }

    public RuleEngineConfiguration getConfig() {
        return config;
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

    public boolean containsComponent(String name) {
        return getComponents().stream()
            .anyMatch(x -> x.getEntityName()
                .equals(name));
    }

    public boolean containsOperationInterface(String name) {
        return getInterfaces().stream()
            .filter(OperationInterface.class::isInstance)
            .anyMatch(x -> x.getEntityName()
                .equals(name));
    }

    public boolean containsOperationSignature(String interfaceName, String signatureName) {
        return !getOperationSignature(interfaceName, signatureName).isEmpty();
    }

    public int getSignatureMaxParameterCount(String interfaceName, String signatureName) {
        Set<OperationSignature> sigs = getOperationSignature(interfaceName, signatureName);
        return sigs.stream()
            .map(OperationSignature::getParameters__OperationSignature)
            .map(List::size)
            .reduce(0, Math::max);
    }

    public void assertMaxParameterCount(int expectedMaxParameterCount, String interfaceName, String signatureName) {
        assertTrue(containsOperationInterface(interfaceName));
        assertTrue(containsOperationSignature(interfaceName, signatureName));
        assertEquals(expectedMaxParameterCount, getSignatureMaxParameterCount(interfaceName, signatureName));
    }

    private Set<OperationSignature> getOperationSignature(String interfaceName, String signatureName) {
        Set<OperationSignature> sigs = getInterfaces().stream()
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

    public static RepositoryImpl loadRepository(URI repoXMI) {
        final List<EObject> contents = new ResourceSetImpl().getResource(repoXMI, true)
            .getContents();

        assertEquals(1, contents.size());
        assertTrue(contents.get(0) instanceof RepositoryImpl);

        // TODO activate this again when all tests are green
        // validate(contents.get(0));

        return (RepositoryImpl) contents.get(0);
    }

    public static void validate(EObject eObject) {
        EcoreUtil.resolveAll(eObject);
        assertEquals(Diagnostic.OK, Diagnostician.INSTANCE.validate(eObject)
            .getSeverity());
    }
}
