package org.palladiosimulator.retriever.test.integration;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.palladiosimulator.generator.fluent.shared.util.ModelLoader;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.reliability.FailureType;
import org.palladiosimulator.pcm.repository.CompositeComponent;
import org.palladiosimulator.pcm.repository.DataType;
import org.palladiosimulator.pcm.repository.Interface;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryComponent;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.retriever.core.configuration.RetrieverConfigurationImpl;
import org.palladiosimulator.retriever.core.workflow.RetrieverJob;
import org.palladiosimulator.retriever.extraction.blackboard.RetrieverBlackboard;
import org.palladiosimulator.retriever.extraction.engine.RetrieverConfiguration;
import org.palladiosimulator.retriever.extraction.engine.Rule;
import org.palladiosimulator.retriever.extraction.engine.ServiceConfiguration;

import com.google.common.collect.Sets;

import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

@TestInstance(Lifecycle.PER_CLASS)
abstract class CaseStudyTest {
    public static final URI TEST_DIR = CommonPlugin
        .asLocalURI(URI.createFileURI(URI.decode(new File("res").getAbsolutePath())));

    private URI outDir;

    public static void validate(EObject eObject) {
        EcoreUtil.resolveAll(eObject);
        assertEquals(Diagnostic.OK, Diagnostician.INSTANCE.validate(eObject)
            .getSeverity());
    }

    // Seperate instances for every child test
    private final Logger logger = Logger.getLogger(this.getClass());

    private final RetrieverConfiguration config = new RetrieverConfigurationImpl();
    private final RetrieverJob retrieverJob;
    private final boolean executedSuccessfully;
    private Repository repository;
    private System system;
    private ResourceEnvironment resourceEnvironment;
    private Allocation allocation;
    private final Set<Rule> rules;

    /**
     * Sets up a generic case study test for Retriever.
     *
     * @param projectDirectory
     *            the name of the project directory that will be analyzed
     * @param rules
     *            the rules to execute
     */
    protected CaseStudyTest(String projectDirectory, Rule... rules) {
        this.rules = Set.of(rules);

        outDir = TEST_DIR.appendSegment("out")
            .appendSegment(this.getClass()
                .getSimpleName());

        config.setInputFolder(TEST_DIR.appendSegments(projectDirectory.split("/")));
        config.setOutputFolder(outDir);

        ServiceConfiguration<Rule> ruleConfig = config.getConfig(Rule.class);
        for (Rule rule : rules) {
            ruleConfig.select(rule);
        }

        retrieverJob = new RetrieverJob(config);

        boolean executedSuccessfully;
        try {
            retrieverJob.execute(new NullProgressMonitor());
            executedSuccessfully = true;
        } catch (JobFailedException | UserCanceledException e) {
            logger.error(e);
            executedSuccessfully = false;
        }
        this.executedSuccessfully = executedSuccessfully;
    }

    // Assertion utilities

    private void assertSuccessfulExecution() {
        assertTrue(executedSuccessfully, "Failed to run Retriever!");
    }

    public void assertMaxParameterCount(int expectedMaxParameterCount, String interfaceName, String operationName) {
        assertInterfaceExists(interfaceName);
        assertOperationExists(interfaceName, operationName);
        assertEquals(expectedMaxParameterCount, getSignatureMaxParameterCount(interfaceName, operationName));
    }

    public void assertComponentExists(String name) {
        assertTrue(getComponents().stream()
            .anyMatch(x -> x.getEntityName()
                .equals(name)), "component \"" + name + "\" must exist");
    }

    public void assertInterfaceExists(String name) {
        assertTrue(getInterfaces().stream()
            .filter(OperationInterface.class::isInstance)
            .anyMatch(x -> x.getEntityName()
                .equals(name)), "interface \"" + name + "\" must exist");
    }

    public void assertOperationExists(String interfaceName, String operationName) {
        assertFalse(getOperationSignature(interfaceName, operationName).isEmpty(),
                "interface \"" + interfaceName + "\" must contain operation \"" + operationName + "\"");
    }

    public void assertComponentRequiresComponent(String requiringName, String providingName) {
        Optional<RepositoryComponent> requiringComponent = getComponents().stream()
            .filter(x -> x.getEntityName()
                .equals(requiringName))
            .findFirst();
        assertTrue(requiringComponent.isPresent(), "\"" + requiringName + "\" must exist");

        Optional<RepositoryComponent> providingComponent = getComponents().stream()
            .filter(x -> x.getEntityName()
                .equals(providingName))
            .findFirst();
        assertTrue(providingComponent.isPresent(), "\"" + providingName + "\" must exist");

        List<Interface> interfaces = getInterfaces();
        assertFalse(interfaces.isEmpty(), "an interface must exist in order for a component to require another");

        Set<EObject> requiredObjects = requiringComponent.get()
            .getRequiredRoles_InterfaceRequiringEntity()
            .stream()
            .flatMap(x -> x.eCrossReferences()
                .stream())
            .collect(Collectors.toSet());
        assertFalse(requiredObjects.isEmpty(), "\"" + requiringName + "\" must require something");

        Set<Interface> requiredInterfaces = interfaces.stream()
            .filter(requiredObjects::contains)
            .collect(Collectors.toSet());
        assertFalse(requiredInterfaces.isEmpty(), "\"" + requiringName + "\" must require an interface");

        Set<EObject> providedObjects = providingComponent.get()
            .getProvidedRoles_InterfaceProvidingEntity()
            .stream()
            .flatMap(x -> x.eCrossReferences()
                .stream())
            .collect(Collectors.toSet());
        assertFalse(providedObjects.isEmpty(), "\"" + providingName + "\" must provide something");

        Set<Interface> providedInterfaces = interfaces.stream()
            .filter(providedObjects::contains)
            .collect(Collectors.toSet());
        assertFalse(providedInterfaces.isEmpty(), "\"" + providingName + "\" must provide an interface");

        assertTrue(requiredInterfaces.stream()
            .anyMatch(providedInterfaces::contains),
                "\"" + requiringName + "\" must require an interface that \"" + providingName + "\" provides");
    }

    public void assertInSameCompositeComponent(String childComponentNameA, String childComponentNameB) {
        Optional<RepositoryComponent> childComponentA = getComponents().stream()
            .filter(x -> x.getEntityName()
                .equals(childComponentNameA))
            .findFirst();
        assertTrue(childComponentA.isPresent(), "\"" + childComponentNameA + "\" must exist");

        Optional<RepositoryComponent> childComponentB = getComponents().stream()
            .filter(x -> x.getEntityName()
                .equals(childComponentNameB))
            .findFirst();
        assertTrue(childComponentB.isPresent(), "\"" + childComponentNameB + "\" must exist");

        List<CompositeComponent> allCompositeComponents = getComponents().stream()
            .filter(CompositeComponent.class::isInstance)
            .map(CompositeComponent.class::cast)
            .collect(Collectors.toList());
        assertFalse(allCompositeComponents.isEmpty(), "There must be a composite component");

        Set<CompositeComponent> compositeComponentsA = allCompositeComponents.stream()
            .filter(x -> x.getAssemblyContexts__ComposedStructure()
                .stream()
                .anyMatch(y -> y.getEncapsulatedComponent__AssemblyContext()
                    .equals(childComponentA.get())))
            .collect(Collectors.toSet());
        assertFalse(compositeComponentsA.isEmpty(), childComponentNameA + " must be part of a composite component");

        Set<CompositeComponent> compositeComponentsB = allCompositeComponents.stream()
            .filter(x -> x.getAssemblyContexts__ComposedStructure()
                .stream()
                .anyMatch(y -> y.getEncapsulatedComponent__AssemblyContext()
                    .equals(childComponentB.get())))
            .collect(Collectors.toSet());
        assertFalse(compositeComponentsB.isEmpty(), childComponentNameB + " must be part of a composite component");

        assertTrue(compositeComponentsA.stream()
            .anyMatch(compositeComponentsB::contains),
                childComponentNameA + " and " + childComponentNameB + " must be part of the same composite component");
    }

    public void assertComponentProvidesOperation(String componentName, String interfaceName, String operationName) {
        Optional<RepositoryComponent> component = getComponents().stream()
            .filter(x -> x.getEntityName()
                .equals(componentName))
            .findFirst();
        assertTrue(component.isPresent(), "Component \"" + componentName + "\" must exist");

        List<Interface> interfaces = getInterfaces();
        assertFalse(interfaces.isEmpty(), "an interface must exist in order for a component to provide an operation");

        Set<EObject> providedObjects = component.get()
            .getProvidedRoles_InterfaceProvidingEntity()
            .stream()
            .flatMap(x -> x.eCrossReferences()
                .stream())
            .collect(Collectors.toSet());
        assertFalse(providedObjects.isEmpty(), "\"" + componentName + "\" must provide something");

        Set<Interface> providedInterfaces = interfaces.stream()
            .filter(providedObjects::contains)
            .collect(Collectors.toSet());
        assertFalse(providedInterfaces.isEmpty(), "\"" + componentName + "\" must provide an interface");

        Set<Interface> specifiedInterfaces = providedInterfaces.stream()
            .filter(x -> x.getEntityName()
                .equals(interfaceName))
            .collect(Collectors.toSet());
        assertFalse(specifiedInterfaces.isEmpty(),
                "\"" + componentName + "\" must provide interface \"" + interfaceName + "\"");

        assertOperationExists(interfaceName, operationName);
    }

    // Getters

    public RetrieverConfiguration getConfig() {
        assertSuccessfulExecution();
        return config;
    }

    public List<RepositoryComponent> getComponents() {
        assertSuccessfulExecution();
        return Collections.unmodifiableList(repository.getComponents__Repository());
    }

    public List<DataType> getDatatypes() {
        assertSuccessfulExecution();
        return Collections.unmodifiableList(repository.getDataTypes__Repository());
    }

    public List<FailureType> getFailuretypes() {
        assertSuccessfulExecution();
        return Collections.unmodifiableList(repository.getFailureTypes__Repository());
    }

    public List<Interface> getInterfaces() {
        assertSuccessfulExecution();
        return Collections.unmodifiableList(repository.getInterfaces__Repository());
    }

    public Repository getRepository() {
        assertSuccessfulExecution();
        assertNotNull(repository);
        return repository;
    }

    public System getSystem() {
        assertSuccessfulExecution();
        assertNotNull(system);
        return system;
    }

    public ResourceEnvironment getResourceEnvironment() {
        assertSuccessfulExecution();
        assertNotNull(resourceEnvironment);
        return resourceEnvironment;
    }

    public Allocation getAllocation() {
        assertSuccessfulExecution();
        assertNotNull(allocation);
        return allocation;
    }

    private Set<OperationSignature> getOperationSignature(String interfaceName, String signatureName) {
        return getInterfaces().stream()
            .filter(OperationInterface.class::isInstance)
            .map(OperationInterface.class::cast)
            .filter(x -> x.getEntityName()
                .equals(interfaceName))
            .map(x -> x.getSignatures__OperationInterface()
                .stream()
                .filter(y -> {
                    // Ignore uniqueness postfix
                    String name = y.getEntityName();
                    int postfixStart = name.indexOf('$');
                    if (postfixStart > -1) {
                        return name.substring(0, postfixStart)
                            .equals(signatureName);
                    }
                    return name.equals(signatureName);
                })
                .collect(Collectors.toSet()))
            .reduce(new HashSet<>(), Sets::union);
    }

    public RetrieverBlackboard getBlackboard() {
        assertSuccessfulExecution();
        return retrieverJob.getBlackboard();
    }

    public Set<Rule> getRules() {
        return Collections.unmodifiableSet(rules);
    }

    public int getSignatureMaxParameterCount(String interfaceName, String signatureName) {
        final Set<OperationSignature> sigs = getOperationSignature(interfaceName, signatureName);
        return sigs.stream()
            .map(OperationSignature::getParameters__OperationSignature)
            .map(List::size)
            .reduce(0, Math::max);
    }

    // Resource loading

    protected enum Artifacts {
        RETRIEVER, MOCORE,
    }

    protected void loadArtifacts(Artifacts artifacts) {
        assertSuccessfulExecution();

        switch (artifacts) {
        case RETRIEVER:
            repository = ModelLoader.loadRepository(outDir.appendSegment("pcm.repository")
                .toString());
            system = null;
            resourceEnvironment = null;
            allocation = null;
            break;
        case MOCORE:
            String fileName = config.getInputFolder()
                .lastSegment();
            if (fileName.isEmpty()) {
                fileName = config.getInputFolder()
                    .trimSegments(1)
                    .lastSegment();
            }
            String mocoreBase = outDir.appendSegment(fileName)
                .toString() + ".";
            repository = ModelLoader.loadRepository(mocoreBase + "repository");
            system = ModelLoader.loadSystem(mocoreBase + "system");
            resourceEnvironment = ModelLoader.loadResourceEnvironment(mocoreBase + "resourceenvironment");
            allocation = ModelLoader.loadAllocation(mocoreBase + "allocation");
            break;
        default:
            throw new IllegalArgumentException("Unhandled artifact type!");
        }
    }

    // Template methods

    void testRetrieverRepository() {
    }

    void testRetrieverSeff() {
    }

    void testMoCoReRepository() {
    }

    void testMoCoReSeff() {
    }

    void testMoCoReSystem() {
    }

    void testMoCoReResourceEnvironment() {
    }

    void testMoCoReAllocation() {
    }

    // Tests

    @Test
    void retrieverRepository() {
        loadArtifacts(Artifacts.RETRIEVER);
        testRetrieverRepository();
    }

    @Test
    void retrieverSeff() {
        loadArtifacts(Artifacts.RETRIEVER);
        testRetrieverSeff();
    }

    @Test
    @Disabled("There are no tests for MoCoRe yet")
    void moCoReRepository() {
        loadArtifacts(Artifacts.MOCORE);
        testMoCoReRepository();
    }

    @Test
    @Disabled("There are no tests for MoCoRe yet")
    void moCoReSeff() {
        loadArtifacts(Artifacts.MOCORE);
        testMoCoReSeff();
    }

    @Test
    @Disabled("There are no tests for MoCoRe yet")
    void moCoReSystem() {
        loadArtifacts(Artifacts.MOCORE);
        testMoCoReSystem();
    }

    @Test
    @Disabled("There are no tests for MoCoRe yet")
    void moCoReResourceEnvironment() {
        loadArtifacts(Artifacts.MOCORE);
        testMoCoReResourceEnvironment();
    }

    @Test
    @Disabled("There are no tests for MoCoRe yet")
    void moCoReAllocation() {
        loadArtifacts(Artifacts.MOCORE);
        testMoCoReAllocation();
    }
}
