package org.palladiosimulator.somox.analyzer.rules.engine.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EcoreUtil;
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
import org.palladiosimulator.somox.discoverer.JavaDiscoverer;

import com.google.common.collect.Sets;

import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

abstract class RuleEngineTest {
	public static final URI TEST_DIR = CommonPlugin
			.asLocalURI(URI.createFileURI(URI.decode(new File("res").getAbsolutePath())));

	public static final URI OUT_DIR = TEST_DIR.appendSegment("out");

	protected static URI getOutputDirectory() {
		return OUT_DIR.appendSegment("jdt");
	}

	public static RepositoryImpl loadRepository(URI repoXMI) {
		final List<EObject> contents = new ResourceSetImpl().getResource(repoXMI, true).getContents();

		assertEquals(1, contents.size());
		assertTrue(contents.get(0) instanceof RepositoryImpl);

		// TODO activate this again when SEFF is included
		// validate(contents.get(0));

		return (RepositoryImpl) contents.get(0);
	}

	public static void validate(EObject eObject) {
		EcoreUtil.resolveAll(eObject);
		assertEquals(Diagnostic.OK, Diagnostician.INSTANCE.validate(eObject).getSeverity());
	}

	// Seperate instances for every child test
	private final Logger logger = Logger.getLogger(this.getClass());

	private boolean isCreated;
	private List<RepositoryComponent> components;
	private final RuleEngineConfiguration config = new RuleEngineConfiguration();
	private List<DataType> datatypes;
	private List<FailureType> failuretypes;
	private List<Interface> interfaces;
	private RepositoryImpl repository;
	private final Set<DefaultRule> rules;

	/**
	 * Tests the basic functionality of the RuleEngineAnalyzer. Requires it to
	 * execute without an exception and produce an output file.
	 *
	 * @param projectDirectory the name of the project directory that will be
	 *                         analyzed
	 */
	protected RuleEngineTest(String projectDirectory, DefaultRule... rules) {
		final RuleEngineBlackboard jdtBlackboard = new RuleEngineBlackboard();
		final RuleEngineAnalyzer jdtAnalyzer = new RuleEngineAnalyzer(jdtBlackboard);
		final Discoverer jdtDiscoverer = new JavaDiscoverer();

		this.rules = Set.of(rules);

		config.setInputFolder(TEST_DIR.appendSegments(projectDirectory.split("/")));
		config.setOutputFolder(getOutputDirectory());
		config.setUseEMFTextParser(false);
		config.setSelectedRules(this.rules);

		try {
			jdtDiscoverer.create(config, jdtBlackboard).execute(null);
			jdtAnalyzer.analyze(config, null);
			isCreated = true;
		} catch (RuleEngineException | JobFailedException | UserCanceledException e) {
			isCreated = false;
		}

		final String jdtRepoPath = getOutputDirectory().appendSegment("pcm.repository").devicePath();
		assertTrue(!isCreated || new File(jdtRepoPath).exists());

		if (isCreated) {
			repository = loadRepository(URI.createFileURI(jdtRepoPath));
		}
		if (isCreated) {
			components = repository.getComponents__Repository();
			datatypes = repository.getDataTypes__Repository();
			failuretypes = repository.getFailureTypes__Repository();
			interfaces = repository.getInterfaces__Repository();
		}
	}

	private void assertCreated() {
		assertTrue(isCreated, "Failed to create model using JavaDiscoverer!");
	}

	public void assertMaxParameterCount(int expectedMaxParameterCount, String interfaceName, String signatureName) {
		assertTrue(containsOperationInterface(interfaceName));
		assertTrue(containsOperationSignature(interfaceName, signatureName));
		assertEquals(expectedMaxParameterCount, getSignatureMaxParameterCount(interfaceName, signatureName));
	}

	@AfterEach
	void cleanUp() {
		final File target = new File(OUT_DIR.devicePath(), this.getClass().getSimpleName() + ".repository");
		if (!target.delete()) {
			logger.error("Could not save delete repository \"" + target.getAbsolutePath() + "\"!");
		}
		if (!new File(OUT_DIR.devicePath(), "pcm.repository").renameTo(target)) {
			logger.error("Could not save created repository to \"" + target.getAbsolutePath() + "\"!");
		}
	}

	public boolean containsComponent(String name) {
		return getComponents().stream().anyMatch(x -> x.getEntityName().equals(name));
	}

	public boolean containsOperationInterface(String name) {
		return getInterfaces().stream().filter(OperationInterface.class::isInstance)
				.anyMatch(x -> x.getEntityName().equals(name));
	}

	public boolean containsOperationSignature(String interfaceName, String signatureName) {
		return !getOperationSignature(interfaceName, signatureName).isEmpty();
	}

	public List<RepositoryComponent> getComponents() {
		assertCreated();
		return Collections.unmodifiableList(components);
	}

	public RuleEngineConfiguration getConfig() {
		assertCreated();
		return config;
	}

	public List<DataType> getDatatypes() {
		assertCreated();
		return Collections.unmodifiableList(datatypes);
	}

	public List<FailureType> getFailuretypes() {
		assertCreated();
		return Collections.unmodifiableList(failuretypes);
	}

	public List<Interface> getInterfaces() {
		assertCreated();
		return Collections.unmodifiableList(interfaces);
	}

	private Set<OperationSignature> getOperationSignature(String interfaceName, String signatureName) {
		return getInterfaces().stream().filter(OperationInterface.class::isInstance).map(OperationInterface.class::cast)
				.filter(x -> x.getEntityName().equals(interfaceName))
				.map(x -> x.getSignatures__OperationInterface().stream()
						.filter(y -> y.getEntityName().equals(signatureName)).collect(Collectors.toSet()))
				.reduce(new HashSet<>(), Sets::union);
	}

	public RepositoryImpl getRepo() {
		assertCreated();
		return repository;
	}

	public Set<DefaultRule> getRules() {
		return Collections.unmodifiableSet(rules);
	}

	public int getSignatureMaxParameterCount(String interfaceName, String signatureName) {
		final Set<OperationSignature> sigs = getOperationSignature(interfaceName, signatureName);
		return sigs.stream().map(OperationSignature::getParameters__OperationSignature).map(List::size).reduce(0,
				Math::max);
	}

	abstract void test();
}
