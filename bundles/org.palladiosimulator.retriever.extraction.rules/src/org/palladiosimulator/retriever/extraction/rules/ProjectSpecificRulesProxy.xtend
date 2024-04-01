package org.palladiosimulator.retriever.extraction.rules

import org.palladiosimulator.retriever.extraction.engine.Rule
import org.palladiosimulator.retriever.extraction.blackboard.RetrieverBlackboard
import java.nio.file.Path
import java.util.Optional
import org.palladiosimulator.retriever.extraction.engine.RetrieverConfiguration
import java.util.Set
import org.eclipse.xtend.core.compiler.batch.XtendBatchCompiler
import java.net.URLClassLoader
import java.io.File

class ProjectSpecificRulesProxy implements Rule {
	
	public static final String RULE_ID = "org.palladiosimulator.retriever.extraction.rules.project_specific"
	public static final String LOADED_CLASS_NAME = "org.palladiosimulator.retriever.extraction.rules.ProjectSpecificRules"
	public static final String RULE_PATH_KEY = "xtend_file_path"

	Optional<Rule> innerRule = Optional.empty;
	
	override create(RetrieverConfiguration config, RetrieverBlackboard blackboard) {
		val rulePath = config.getConfig(Rule).getConfig(RULE_ID, RULE_PATH_KEY)
		val outputPath = "TODO" // TODO
		
		val compiler = new XtendBatchCompiler()
		compiler.sourcePath = rulePath
		compiler.outputPath = outputPath
		if (compiler.compile()) {
			val outputUrl = new File(outputPath).toURI.toURL
			val classLoader = new URLClassLoader(#[outputUrl])
			val loadedClass = classLoader.loadClass(LOADED_CLASS_NAME)
			val ruleInstance = loadedClass.getConstructor().newInstance()
			// TODO: handle failures
			innerRule = Optional.of(Rule.cast(ruleInstance))
		}
		
		if (innerRule.isPresent()) {
			innerRule.get().create(config, blackboard)
		} else {
			Rule.super.create(config, blackboard)
			// TODO: Fail or log error
		}
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