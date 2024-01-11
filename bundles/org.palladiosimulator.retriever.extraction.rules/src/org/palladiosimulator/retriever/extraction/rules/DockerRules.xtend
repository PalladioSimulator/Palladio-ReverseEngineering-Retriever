package org.palladiosimulator.retriever.extraction.rules

import org.palladiosimulator.retriever.extraction.blackboard.RuleEngineBlackboard
import java.nio.file.Path;
import java.util.HashSet
import org.eclipse.jdt.core.dom.CompilationUnit
import java.util.Set
import org.palladiosimulator.retriever.extraction.engine.Rule

class DockerRules implements Rule {

	static final String RULE_ID = "org.palladiosimulator.somox.analyzer.rules.impl.docker"
	static final String JAVA_DISCOVERER_ID = "org.palladiosimulator.somox.discoverer.java"
	static final String DOCKER_FILE_NAME = "Dockerfile";

	override processRules(RuleEngineBlackboard blackboard, Path path) {
		if (path !== null && path.fileName.toString().equals(DOCKER_FILE_NAME)) {

			// Add all file system children as associated compilation units
			var children = new HashSet<CompilationUnit>();
			var parentPath = path.parent;
			for (entry : blackboard.getDiscoveredFiles(JAVA_DISCOVERER_ID, typeof(CompilationUnit)).entrySet) {
				if (entry.key.startsWith(parentPath)) {
					children.add(entry.value);
				}
			}
			blackboard.addSystemAssociations(path, children);
		}
	}

	override isBuildRule() {
		true
	}

	override getConfigurationKeys() {
		return Set.of
	}

	override getID() {
		RULE_ID
	}

	override getName() {
		"Docker Rules"
	}

	override getRequiredServices() {
		return Set.of(JAVA_DISCOVERER_ID)
	}

	override getDependentServices() {
		Set.of
	}
}
