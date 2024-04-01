package org.palladiosimulator.retriever.extraction.rules

import org.palladiosimulator.retriever.extraction.engine.Rule
import org.palladiosimulator.retriever.extraction.blackboard.RetrieverBlackboard
import java.nio.file.Path
import java.util.Optional
import org.palladiosimulator.retriever.extraction.engine.RetrieverConfiguration
import java.util.Set

class ProjectSpecificRules implements Rule {
	
	public static final String RULE_ID = "org.palladiosimulator.retriever.extraction.rules.spring.cloudgateway"
	public static final String RULE_PATH_KEY = "xtend_file_path"

	Optional<Rule> innerRule = Optional.empty;
	
	override create(RetrieverConfiguration config, RetrieverBlackboard blackboard) {
		// TODO: load and build innerRule
	}
	
	override isBuildRule() {
		// project-specific rules may not be build rules
		false
	}
	
	override processRules(RetrieverBlackboard blackboard, Path path) {
		innerRule.ifPresent[x|x.processRules(blackboard, path)]
	}
	
	override getConfigurationKeys() {
		Set.of(RULE_PATH_KEY)
	}
	
	override getDependentServices() {
		// project-specific rules may (currently) not be a requirement for any other services
		Set.of
	}
	
	override getID() {
		RULE_ID
	}
	
	override getName() {
		"Project-Specific Rules"
	}
	
	override getRequiredServices() {
		// special value signaling dependency on all other rules
		Set.of(null)
	}
	
}