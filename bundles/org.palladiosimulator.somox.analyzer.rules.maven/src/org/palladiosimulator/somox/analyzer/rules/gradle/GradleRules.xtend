package org.palladiosimulator.somox.analyzer.rules.gradle

import org.palladiosimulator.somox.analyzer.rules.engine.IRule

import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard
import java.nio.file.Path;
import java.util.HashSet
import org.eclipse.jdt.core.dom.CompilationUnit

class GradleRules extends IRule {

	static final String JAVA_DISCOVERER_ID = "org.palladiosimulator.somox.discoverer.java";
	static final String GRADLE_FILE_NAME = "build.gradle";

	new(RuleEngineBlackboard blackboard) {
		super(blackboard)
	}

	override boolean processRules(Path path) {
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

			return true;
		}
		return false;
	}
}
