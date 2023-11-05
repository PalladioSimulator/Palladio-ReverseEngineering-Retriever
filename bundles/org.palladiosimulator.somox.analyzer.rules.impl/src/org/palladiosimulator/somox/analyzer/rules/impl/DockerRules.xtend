package org.palladiosimulator.somox.analyzer.rules.impl

import org.palladiosimulator.somox.analyzer.rules.engine.IRule

import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard
import java.nio.file.Path;
import java.util.HashSet
import org.eclipse.jdt.core.dom.CompilationUnit
import org.palladiosimulator.somox.analyzer.rules.engine.RuleEngineConfiguration
import java.util.Set

class DockerRules implements IRule {

    static final String RULE_ID = "org.palladiosimulator.somox.analyzer.rules.impl.docker"
    static final String JAVA_DISCOVERER_ID = "org.palladiosimulator.somox.discoverer.java"
	static final String DOCKER_FILE_NAME = "Dockerfile";
	
	override boolean processRules(RuleEngineBlackboard blackboard, Path path) {
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
			
			return true;
		}
		return false;
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
	
}
