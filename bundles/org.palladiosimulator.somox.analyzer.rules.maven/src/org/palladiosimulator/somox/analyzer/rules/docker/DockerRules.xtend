package org.palladiosimulator.somox.analyzer.rules.docker

import org.palladiosimulator.somox.analyzer.rules.engine.IRule

import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard
import java.nio.file.Path;
import java.util.HashSet
import org.palladiosimulator.somox.analyzer.rules.blackboard.CompilationUnitWrapper

class DockerRules extends IRule {
	static final String DOCKER_FILE_NAME = "Dockerfile";
	
	new(RuleEngineBlackboard blackboard) {
		super(blackboard)
	}
	
	override boolean processRules(Path path) {
		if (path !== null && path.fileName.toString().equals(DOCKER_FILE_NAME)) {
			
			// Add all file system children as associated compilation units
			var children = new HashSet<CompilationUnitWrapper>();
			var parentPath = path.parent;
			for (unit : blackboard.compilationUnits) {
				var isChild = false;
				for (unitPath : blackboard.getCompilationUnitLocations(unit)) {
					if (unitPath.startsWith(parentPath)) {
						// The compilation unit is a child of this build file
						isChild = true;
					}
				}
				if (isChild) {
					children.add(unit);
				}
			}
			blackboard.addSystemAssociations(path, children);
			
			return true;
		}
		return false;
	}
}
