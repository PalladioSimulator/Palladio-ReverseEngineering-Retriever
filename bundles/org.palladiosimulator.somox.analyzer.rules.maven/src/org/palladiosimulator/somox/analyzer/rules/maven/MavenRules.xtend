package org.palladiosimulator.somox.analyzer.rules.maven

import org.palladiosimulator.somox.analyzer.rules.engine.IRule

import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard
import java.nio.file.Path;
import java.util.HashSet
import org.eclipse.jdt.core.dom.CompilationUnit

class MavenRules extends IRule {
	static final String MAVEN_FILE_NAME = "pom.xml";

	new(RuleEngineBlackboard blackboard) {
		super(blackboard)
	}

	override boolean processRules(Path path) {
		if (path !== null && path.fileName.toString().equals(MAVEN_FILE_NAME)) {

			// Add all file system children as associated compilation units
			var children = new HashSet<CompilationUnit>();
			var parentPath = path.parent;
			for (unit : blackboard.compilationUnits) {
				var unitPath = blackboard.getCompilationUnitLocation(unit);
				if (unitPath !== null && unitPath.startsWith(parentPath)) {
					// The compilation unit is a child of this build file
					children.add(unit);
				}
			}
			blackboard.addSystemAssociations(path, children);

			return true;
		}
		return false;
	}
}
