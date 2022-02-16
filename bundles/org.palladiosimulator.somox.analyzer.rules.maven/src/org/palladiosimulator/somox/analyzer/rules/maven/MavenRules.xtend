package org.palladiosimulator.somox.analyzer.rules.maven

import org.palladiosimulator.somox.analyzer.rules.engine.IRule

import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard
import java.nio.file.Path;
import java.util.HashSet
import org.emftext.language.java.containers.impl.CompilationUnitImpl

class MavenRules extends IRule {
	static final String MAVEN_FILE_NAME = "pom.xml";
	
	new(RuleEngineBlackboard blackboard) {
		super(blackboard)
	}
	
	override boolean processRules(Path path) {
		if (path !== null && path.fileName.toString().equals(MAVEN_FILE_NAME)) {
			
			// Add all file system children as associated compilation units
			var children = new HashSet<CompilationUnitImpl>();
			var parentPath = path.parent;
			for (compUnit : blackboard.compilationUnits) {
				var isChild = false;
				for (compUnitPath : blackboard.getCompilationUnitLocations(compUnit)) {
					if (compUnitPath.startsWith(parentPath)) {
						// The compilation unit is a child of this build file
						isChild = true;
					}
				}
				if (isChild) {
					children.add(compUnit);
				}
			}
			blackboard.addSystemAssociations(path, children);
			
			return true;
		}
		return false;
	}
}
