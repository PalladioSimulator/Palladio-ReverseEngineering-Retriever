package org.palladiosimulator.somox.analyzer.rules.engine.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.emftext.language.java.containers.CompilationUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineConfiguration;
import org.palladiosimulator.somox.analyzer.rules.workflow.JdtParserJob;

import de.uka.ipd.sdq.workflow.Workflow;

class JdtParserJobTest {

	Path directoryunderTest = Paths.get("res");

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	@AfterEach
	void cleanUp() throws Exception {
		try (Stream<Path> walk = Files.walk(directoryunderTest, 1)) {
			walk.filter(Files::isRegularFile).filter(p -> p.toString().endsWith("." + JdtParserJob.MODEL_EXTENSION))
				.anyMatch(p -> {
					try {
						return Files.deleteIfExists(p);
					} catch (final IOException e) {
						return false;
					}
				});
		}
	}

	Resource getResource(String projectName) {
		final Path path = Paths.get(directoryunderTest.toString(), projectName);
		assertTrue(path.toFile().isDirectory());
		final URI uri = URI.createURI(path.toUri().normalize().getPath());

		final RuleEngineBlackboard blackboard = new RuleEngineBlackboard();
		final RuleEngineConfiguration configuration = new RuleEngineConfiguration();
		configuration.setInputFolder(uri);

		new Workflow(new JdtParserJob(configuration, blackboard)).run();

		assertTrue(blackboard.getPartitionIds().size() == 1);
		final String id = blackboard.getPartitionIds().iterator().next();
		assertTrue(id.startsWith(JdtParserJob.JOB_NAME));

		final Object partition = blackboard.getPartition(id);
		assertTrue(partition instanceof URI);

		blackboard.removePartition(id);
		assertTrue(blackboard.getPartitionIds().size() == 0);

		return JdtParserJob.getResource((URI) partition);
	}

	@Test
	void testBasicProject() {
		final List<EObject> resources = getResource("BasicProject").getContents();
		assertEquals(1, resources.size());
		final EObject resource = resources.get(0);
		assertTrue(resource instanceof CompilationUnit);
		final CompilationUnit unit = (CompilationUnit) resource;
		final org.emftext.language.java.classifiers.Class classifier = unit.getContainedClass();
		assertEquals("Main", classifier.getName());
	}

}
