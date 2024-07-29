package org.palladiosimulator.retriever.extraction.rules

import java.nio.file.Path;
import java.util.Set
import org.palladiosimulator.retriever.services.blackboard.RetrieverBlackboard
import org.palladiosimulator.retriever.services.Rule
import org.palladiosimulator.retriever.extraction.engine.PCMDetector

class TeammatesRules implements Rule {

	static final String RULE_ID = "org.palladiosimulator.retriever.extraction.rules.teammates"
	static final String JAVA_DISCOVERER_ID = "org.palladiosimulator.retriever.extraction.discoverers.java"
	static final String JAX_RS_RULES_ID = "org.palladiosimulator.retriever.extraction.rules.jax_rs"

	override processRules(RetrieverBlackboard blackboard, Path path) {
		val pcmDetector = blackboard.PCMDetector as PCMDetector;
		pcmDetector.addToBlacklist("teammates.common.util.Logger");
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
		"Teammates Rules"
	}

	override getRequiredServices() {
		return Set.of(JAVA_DISCOVERER_ID)
	}

	override getDependentServices() {
		Set.of(JAX_RS_RULES_ID)
	}
}
