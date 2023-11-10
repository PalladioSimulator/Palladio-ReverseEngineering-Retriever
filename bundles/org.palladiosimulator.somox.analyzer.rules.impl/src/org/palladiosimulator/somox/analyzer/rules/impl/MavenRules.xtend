package org.palladiosimulator.somox.analyzer.rules.impl

import org.palladiosimulator.somox.analyzer.rules.engine.IRule

import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard
import java.nio.file.Path;
import java.util.HashSet
import org.eclipse.jdt.core.dom.CompilationUnit
import java.util.Set

class MavenRules implements IRule {
	
	static final String RULE_ID = "org.palladiosimulator.somox.analyzer.rules.impl.maven"
	static final String JAVA_DISCOVERER_ID = "org.palladiosimulator.somox.discoverer.java";
	static final String MAVEN_FILE_NAME = "pom.xml";

	override processRules(RuleEngineBlackboard blackboard, Path path) {
		if (path !== null && path.fileName.toString().equals(MAVEN_FILE_NAME)) {

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
		return "Maven Rules"
	}
	
}
