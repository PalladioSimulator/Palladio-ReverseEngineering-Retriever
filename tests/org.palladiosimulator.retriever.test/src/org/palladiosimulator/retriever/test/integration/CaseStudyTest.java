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

    private final URI outDir;

    public static void validate(final EObject eObject) {
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
    protected CaseStudyTest(final String projectDirectory, final Rule... rules) {
        this.rules = Set.of(rules);

        this.outDir = TEST_DIR.appendSegment("out")
            .appendSegment(this.getClass()
                .getSimpleName());

        this.config.setInputFolder(TEST_DIR.appendSegments(projectDirectory.split("/")));
        this.config.setOutputFolder(this.outDir);

        final ServiceConfiguration<Rule> ruleConfig = this.config.getConfig(Rule.class);
        for (final Rule rule : rules) {
            ruleConfig.select(rule);
        }

        this.retrieverJob = new RetrieverJob(this.config);

        boolean executedSuccessfully;
        try {
            this.retrieverJob.execute(new NullProgressMonitor());
            executedSuccessfully = true;
        } catch (JobFailedException | UserCanceledException e) {
            this.logger.error(e);
            executedSuccessfully = false;
        }
        this.executedSuccessfully = executedSuccessfully;
    }

    // Assertion utilities

    private void assertSuccessfulExecution() {
        assertTrue(this.executedSuccessfully, "Failed to run Retriever!");
    }

    public void assertMaxParameterCount(final int expectedMaxParameterCount, final String interfaceName,
            final String operationName) {
        this.assertInterfaceExists(interfaceName);
        this.assertOperationExists(interfaceName, operationName);
        assertEquals(expectedMaxParameterCount, this.getSignatureMaxParameterCount(interfaceName, operationName));
    }

    public void assertComponentExists(final String name) {
        assertTrue(this.getComponents()
            .stream()
            .anyMatch(x -> x.getEntityName()
                .equals(name)), "component \"" + name + "\" must exist");
    }

    public void assertInterfaceExists(final String name) {
        assertTrue(this.getInterfaces()
            .stream()
            .filter(OperationInterface.class::isInstance)
            .anyMatch(x -> x.getEntityName()
                .equals(name)), "interface \"" + name + "\" must exist");
    }

    public void assertOperationExists(final String interfaceName, final String operationName) {
        assertFalse(this.getOperationSignature(interfaceName, operationName)
            .isEmpty(), "interface \"" + interfaceName + "\" must contain operation \"" + operationName + "\"");
    }

    public void assertComponentRequiresComponent(final String requiringName, final String providingName) {
        final Optional<RepositoryComponent> requiringComponent = this.getComponents()
            .stream()
            .filter(x -> x.getEntityName()
                .equals(requiringName))
            .findFirst();
        assertTrue(requiringComponent.isPresent(), "\"" + requiringName + "\" must exist");

        final Optional<RepositoryComponent> providingComponent = this.getComponents()
            .stream()
            .filter(x -> x.getEntityName()
                .equals(providingName))
            .findFirst();
        assertTrue(providingComponent.isPresent(), "\"" + providingName + "\" must exist");

        final List<Interface> interfaces = this.getInterfaces();
        assertFalse(interfaces.isEmpty(), "an interface must exist in order for a component to require another");

        final Set<EObject> requiredObjects = requiringComponent.get()
            .getRequiredRoles_InterfaceRequiringEntity()
            .stream()
            .flatMap(x -> x.eCrossReferences()
                .stream())
            .collect(Collectors.toSet());
        assertFalse(requiredObjects.isEmpty(), "\"" + requiringName + "\" must require something");

        final Set<Interface> requiredInterfaces = interfaces.stream()
            .filter(requiredObjects::contains)
            .collect(Collectors.toSet());
        assertFalse(requiredInterfaces.isEmpty(), "\"" + requiringName + "\" must require an interface");

        final Set<EObject> providedObjects = providingComponent.get()
            .getProvidedRoles_InterfaceProvidingEntity()
            .stream()
            .flatMap(x -> x.eCrossReferences()
                .stream())
            .collect(Collectors.toSet());
        assertFalse(providedObjects.isEmpty(), "\"" + providingName + "\" must provide something");

        final Set<Interface> providedInterfaces = interfaces.stream()
            .filter(providedObjects::contains)
            .collect(Collectors.toSet());
        assertFalse(providedInterfaces.isEmpty(), "\"" + providingName + "\" must provide an interface");

        assertTrue(requiredInterfaces.stream()
            .anyMatch(providedInterfaces::contains),
                "\"" + requiringName + "\" must require an interface that \"" + providingName + "\" provides");
    }

    public void assertInSameCompositeComponent(final String childComponentNameA, final String childComponentNameB) {
        final Optional<RepositoryComponent> childComponentA = this.getComponents()
            .stream()
            .filter(x -> x.getEntityName()
                .equals(childComponentNameA))
            .findFirst();
        assertTrue(childComponentA.isPresent(), "\"" + childComponentNameA + "\" must exist");

        final Optional<RepositoryComponent> childComponentB = this.getComponents()
            .stream()
            .filter(x -> x.getEntityName()
                .equals(childComponentNameB))
            .findFirst();
        assertTrue(childComponentB.isPresent(), "\"" + childComponentNameB + "\" must exist");

        final List<CompositeComponent> allCompositeComponents = this.getComponents()
            .stream()
            .filter(CompositeComponent.class::isInstance)
            .map(CompositeComponent.class::cast)
            .collect(Collectors.toList());
        assertFalse(allCompositeComponents.isEmpty(), "There must be a composite component");

        final Set<CompositeComponent> compositeComponentsA = allCompositeComponents.stream()
            .filter(x -> x.getAssemblyContexts__ComposedStructure()
                .stream()
                .anyMatch(y -> y.getEncapsulatedComponent__AssemblyContext()
                    .equals(childComponentA.get())))
            .collect(Collectors.toSet());
        assertFalse(compositeComponentsA.isEmpty(), childComponentNameA + " must be part of a composite component");

        final Set<CompositeComponent> compositeComponentsB = allCompositeComponents.stream()
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

    public void assertComponentProvidesOperation(final String componentName, final String interfaceName,
            final String operationName) {
        final Optional<RepositoryComponent> component = this.getComponents()
            .stream()
            .filter(x -> x.getEntityName()
                .equals(componentName))
            .findFirst();
        assertTrue(component.isPresent(), "Component \"" + componentName + "\" must exist");

        final List<Interface> interfaces = this.getInterfaces();
        assertFalse(interfaces.isEmpty(), "an interface must exist in order for a component to provide an operation");

        final Set<EObject> providedObjects = component.get()
            .getProvidedRoles_InterfaceProvidingEntity()
            .stream()
            .flatMap(x -> x.eCrossReferences()
                .stream())
            .collect(Collectors.toSet());
        assertFalse(providedObjects.isEmpty(), "\"" + componentName + "\" must provide something");

        final Set<Interface> providedInterfaces = interfaces.stream()
            .filter(providedObjects::contains)
            .collect(Collectors.toSet());
        assertFalse(providedInterfaces.isEmpty(), "\"" + componentName + "\" must provide an interface");

        final Set<Interface> specifiedInterfaces = providedInterfaces.stream()
            .filter(x -> x.getEntityName()
                .equals(interfaceName))
            .collect(Collectors.toSet());
        assertFalse(specifiedInterfaces.isEmpty(),
                "\"" + componentName + "\" must provide interface \"" + interfaceName + "\"");

        this.assertOperationExists(interfaceName, operationName);
    }

    // Getters

    public RetrieverConfiguration getConfig() {
        this.assertSuccessfulExecution();
        return this.config;
    }

    public List<RepositoryComponent> getComponents() {
        this.assertSuccessfulExecution();
        return Collections.unmodifiableList(this.repository.getComponents__Repository());
    }

    public List<DataType> getDatatypes() {
        this.assertSuccessfulExecution();
        return Collections.unmodifiableList(this.repository.getDataTypes__Repository());
    }

    public List<FailureType> getFailuretypes() {
        this.assertSuccessfulExecution();
        return Collections.unmodifiableList(this.repository.getFailureTypes__Repository());
    }

    public List<Interface> getInterfaces() {
        this.assertSuccessfulExecution();
        return Collections.unmodifiableList(this.repository.getInterfaces__Repository());
    }

    public Repository getRepository() {
        this.assertSuccessfulExecution();
        assertNotNull(this.repository);
        return this.repository;
    }

    public System getSystem() {
        this.assertSuccessfulExecution();
        assertNotNull(this.system);
        return this.system;
    }

    public ResourceEnvironment getResourceEnvironment() {
        this.assertSuccessfulExecution();
        assertNotNull(this.resourceEnvironment);
        return this.resourceEnvironment;
    }

    public Allocation getAllocation() {
        this.assertSuccessfulExecution();
        assertNotNull(this.allocation);
        return this.allocation;
    }

    private Set<OperationSignature> getOperationSignature(final String interfaceName, final String signatureName) {
        return this.getInterfaces()
            .stream()
            .filter(OperationInterface.class::isInstance)
            .map(OperationInterface.class::cast)
            .filter(x -> x.getEntityName()
                .equals(interfaceName))
            .map(x -> x.getSignatures__OperationInterface()
                .stream()
                .filter(y -> {
                    // Ignore uniqueness postfix
                    final String name = y.getEntityName();
                    final int postfixStart = name.indexOf('$');
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
        this.assertSuccessfulExecution();
        return this.retrieverJob.getBlackboard();
    }

    public Set<Rule> getRules() {
        return Collections.unmodifiableSet(this.rules);
    }

    public int getSignatureMaxParameterCount(final String interfaceName, final String signatureName) {
        final Set<OperationSignature> sigs = this.getOperationSignature(interfaceName, signatureName);
        return sigs.stream()
            .map(OperationSignature::getParameters__OperationSignature)
            .map(List::size)
            .reduce(0, Math::max);
    }

    // Resource loading

    protected enum Artifacts {
        RETRIEVER, MOCORE,
    }

    protected void loadArtifacts(final Artifacts artifacts) {
        this.assertSuccessfulExecution();

        switch (artifacts) {
        case RETRIEVER:
            this.repository = ModelLoader.loadRepository(this.outDir.appendSegment("pcm.repository")
                .toString());
            this.system = null;
            this.resourceEnvironment = null;
            this.allocation = null;
            break;
        case MOCORE:
            String fileName = this.config.getInputFolder()
                .lastSegment();
            if (fileName.isEmpty()) {
                fileName = this.config.getInputFolder()
                    .trimSegments(1)
                    .lastSegment();
            }
            final String mocoreBase = this.outDir.appendSegment(fileName)
                .toString() + ".";
            this.repository = ModelLoader.loadRepository(mocoreBase + "repository");
            this.system = ModelLoader.loadSystem(mocoreBase + "system");
            this.resourceEnvironment = ModelLoader.loadResourceEnvironment(mocoreBase + "resourceenvironment");
            this.allocation = ModelLoader.loadAllocation(mocoreBase + "allocation");
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
        this.loadArtifacts(Artifacts.RETRIEVER);
        this.testRetrieverRepository();
    }

    @Test
    void retrieverSeff() {
        this.loadArtifacts(Artifacts.RETRIEVER);
        this.testRetrieverSeff();
    }

    @Test
    @Disabled("There are no tests for MoCoRe yet")
    void moCoReRepository() {
        this.loadArtifacts(Artifacts.MOCORE);
        this.testMoCoReRepository();
    }

    @Test
    @Disabled("There are no tests for MoCoRe yet")
    void moCoReSeff() {
        this.loadArtifacts(Artifacts.MOCORE);
        this.testMoCoReSeff();
    }

    @Test
    @Disabled("There are no tests for MoCoRe yet")
    void moCoReSystem() {
        this.loadArtifacts(Artifacts.MOCORE);
        this.testMoCoReSystem();
    }

    @Test
    @Disabled("There are no tests for MoCoRe yet")
    void moCoReResourceEnvironment() {
        this.loadArtifacts(Artifacts.MOCORE);
        this.testMoCoReResourceEnvironment();
    }

    @Test
    @Disabled("There are no tests for MoCoRe yet")
    void moCoReAllocation() {
        this.loadArtifacts(Artifacts.MOCORE);
        this.testMoCoReAllocation();
    }
}
