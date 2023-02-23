package org.palladiosimulator.somox.analyzer.rules.engine.test;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineConfiguration;
import org.palladiosimulator.somox.analyzer.rules.main.RuleEngineAnalyzer;
import org.palladiosimulator.somox.analyzer.rules.main.RuleEngineException;
import org.palladiosimulator.somox.analyzer.rules.trace.ArtifactTraceCreator;
import org.palladiosimulator.somox.analyzer.rules.trace.ArtifactTraceJob;
import org.palladiosimulator.somox.discoverer.Discoverer;
import org.palladiosimulator.somox.discoverer.JavaDiscoverer;

import de.uka.ipd.sdq.workflow.Workflow;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class TraceTest {
	public static final URI TEST_DIR = CommonPlugin
			.asLocalURI(URI.createFileURI(URI.decode(new File("res").getAbsolutePath())));
	public static final URI OUT_DIR = TEST_DIR.appendSegment("out");

	@AfterEach
	void cleanUp() {
		try (final Stream<Path> walk = Files.walk(Paths.get(OUT_DIR.devicePath()))) {
			walk.sorted(Comparator.reverseOrder()).forEach(path -> {
				try {
					Files.delete(path);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public RuleEngineBlackboard setUpBlackboard(RuleEngineConfiguration configuration)
			throws JobFailedException, UserCanceledException, RuleEngineException {
		final RuleEngineBlackboard blackboard = new RuleEngineBlackboard();

		final Discoverer discoverer = new JavaDiscoverer();
		discoverer.create(configuration, blackboard).execute(null);

		final RuleEngineAnalyzer analyzer = new RuleEngineAnalyzer(blackboard);
		analyzer.analyze(configuration, null);

		return blackboard;
	}

	private RuleEngineConfiguration setUpConfiguration(String projectDirectory, DefaultRule... rules) {
		final RuleEngineConfiguration configuration = new RuleEngineConfiguration();
		configuration.setInputFolder(TEST_DIR.appendSegments(projectDirectory.split("/")));
		configuration.setOutputFolder(OUT_DIR);
		configuration.setSelectedRules(Set.of(rules));
		configuration.setUseEMFTextParser(false);
		return configuration;
	}

	private ArtifactTraceCreator setUpCreator(final String project, final DefaultRule... rules)
			throws JobFailedException, UserCanceledException, RuleEngineException {
		final RuleEngineConfiguration configuration = setUpConfiguration(project, rules);
		final RuleEngineBlackboard blackboard = setUpBlackboard(configuration);

		new Workflow(new ArtifactTraceJob(configuration, blackboard)).run();

		final Object partition = blackboard.getPartition(ArtifactTraceJob.ARTIFACT_TRACE_CREATOR);
		assertNotNull(partition);
		assertTrue(partition instanceof ArtifactTraceCreator);
		final ArtifactTraceCreator creator = (ArtifactTraceCreator) partition;

		// TODO Save later
		creator.save(Paths.get(configuration.getOutputFolder().devicePath()), project);

		return creator;
	}

	@Test
	void testACME() {
		final ArtifactTraceCreator creator = assertDoesNotThrow(
				() -> setUpCreator("external/acmeair-1.2.0", DefaultRule.JAX_RS));
		assertNotNull(creator);
	}

	@Test
	void testBasic() {
		final ArtifactTraceCreator creator = assertDoesNotThrow(() -> setUpCreator("BasicProject", DefaultRule.JAX_RS));
		assertNotNull(creator);
	}

	@Test
	void testJaxRs() {
		final ArtifactTraceCreator creator = assertDoesNotThrow(() -> setUpCreator("JaxRsProject", DefaultRule.JAX_RS));
		assertNotNull(creator);
	}

	@Test
	void testPetclinic() {
		final ArtifactTraceCreator creator = assertDoesNotThrow(
				() -> setUpCreator("external/spring-petclinic-microservices-2.3.6", DefaultRule.SPRING));
		assertNotNull(creator);
	}

	@Test
	void testPiggymetrics() {
		final ArtifactTraceCreator creator = assertDoesNotThrow(
				() -> setUpCreator("external/piggymetrics-spring.version.2.0.3", DefaultRule.SPRING));
		assertNotNull(creator);
	}

	@Test
	void testTeaStore() {
		final ArtifactTraceCreator creator = assertDoesNotThrow(
				() -> setUpCreator("external/TeaStore-1.4.1", DefaultRule.SPRING));
		assertNotNull(creator);
	}

}
