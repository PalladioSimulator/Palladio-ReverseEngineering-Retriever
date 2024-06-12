package org.palladiosimulator.retriever.extraction.rules

import java.nio.file.Path;
import java.util.HashSet
import org.eclipse.jdt.core.dom.CompilationUnit
import java.util.Set
import org.palladiosimulator.retriever.services.blackboard.RetrieverBlackboard
import org.palladiosimulator.retriever.services.Rule

class GradleRules implements Rule {

	static final String RULE_ID = "org.palladiosimulator.retriever.extraction.rules.gradle";
	static final String JAVA_DISCOVERER_ID = "org.palladiosimulator.retriever.extraction.discoverers.java";
	static final String GRADLE_FILE_NAME = "build.gradle";

	override processRules(RetrieverBlackboard blackboard, Path path) {
		if (path !== null && path.fileName.toString().equals(GRADLE_FILE_NAME)) {

			// Add all file system children as associated compilation units
			var children = new HashSet<CompilationUnit>();
			var parentPath = path.parent;
			for (entry : blackboard.getDiscoveredFiles(JAVA_DISCOVERER_ID, typeof(CompilationUnit)).entrySet) {
				if (entry.key.startsWith(parentPath)) {
					// The compilation unit is a child of this build file
					children.add(entry.value);
				}
			}
			blackboard.addSystemAssociations(path, children);
		}
	}

	override isBuildRule() {
		return true
	}

	override getConfigurationKeys() {
		return Set.of
	}

	override getID() {
		return RULE_ID
	}

	override getName() {
		return "Gradle Rules"
	}

	override getRequiredServices() {
		return Set.of(JAVA_DISCOVERER_ID)
	}

	override getDependentServices() {
		Set.of
	}
}
