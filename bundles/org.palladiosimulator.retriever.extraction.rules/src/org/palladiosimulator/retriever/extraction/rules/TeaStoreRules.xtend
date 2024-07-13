package org.palladiosimulator.retriever.extraction.rules

import java.nio.file.Path;
import java.util.HashSet
import org.eclipse.jdt.core.dom.CompilationUnit
import java.util.Set
import org.palladiosimulator.retriever.services.blackboard.RetrieverBlackboard
import org.palladiosimulator.retriever.services.Rule
import org.palladiosimulator.retriever.extraction.engine.PCMDetector
import org.palladiosimulator.retriever.extraction.commonalities.CompUnitOrName

class TeaStoreRules implements Rule {

	static final String RULE_ID = "org.palladiosimulator.retriever.extraction.rules.teastore"
	static final String JAVA_DISCOVERER_ID = "org.palladiosimulator.retriever.extraction.discoverers.java";
	static final String DOCKER_DISCOVERER_ID = "org.palladiosimulator.retriever.extraction.discoverers.docker";
	static final String DOCKER_FILE_NAME = "Dockerfile";

	override processRules(RetrieverBlackboard blackboard, Path path) {
		if (path !== null && path.fileName.toString().equals(DOCKER_FILE_NAME)) {
			val parentName = path.parent.fileName.toString()

			// Add all file system children as associated compilation units
			var children = new HashSet<CompilationUnit>();
			var parentPath = path.parent;
			for (entry : blackboard.getDiscoveredFiles(JAVA_DISCOVERER_ID, typeof(CompilationUnit)).entrySet) {
				if (entry.key.startsWith(parentPath)) {
					children.add(entry.value);
				}
			}
			val pcmDetector = blackboard.PCMDetector as PCMDetector;
			for (unit : children) {
				if (pcmDetector.compilationUnits.contains(new CompUnitOrName(unit))) {
					pcmDetector.detectSeparatingIdentifier(new CompUnitOrName(unit), parentName)
					if (pcmDetector.isPartOfComposite(new CompUnitOrName(unit))) {
						pcmDetector.detectPartOfComposite(new CompUnitOrName(unit), parentName)
					}
				}
			}
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
		return "TeaStore Rules"
	}

	override getRequiredServices() {
		return Set.of(JAVA_DISCOVERER_ID, DOCKER_DISCOVERER_ID)
	}

	override getDependentServices() {
		Set.of
	}
}
