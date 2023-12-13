package org.palladiosimulator.somox.analyzer.rules.impl

import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard
import java.nio.file.Path;
import org.eclipse.jdt.core.dom.CompilationUnit
import static org.palladiosimulator.somox.analyzer.rules.engine.RuleHelper.*
import org.palladiosimulator.somox.analyzer.rules.model.CompUnitOrName
import java.util.Set
import org.palladiosimulator.somox.analyzer.rules.engine.Rule
import org.palladiosimulator.somox.analyzer.rules.model.RESTName
import java.util.Optional
import org.palladiosimulator.somox.analyzer.rules.impl.util.RESTHelper

class JaxRSRules implements Rule {

	public static final String RULE_ID = "org.palladiosimulator.somox.analyzer.rules.impl.jax_rs"

	public static final String JAVA_DISCOVERER_ID = "org.palladiosimulator.somox.discoverer.java"

	override processRules(RuleEngineBlackboard blackboard, Path path) {
		val unit = blackboard.getDiscoveredFiles(JAVA_DISCOVERER_ID, typeof(CompilationUnit)).get(path)

		if(unit === null) return;

		processRuleForCompUnit(blackboard, unit)
	}

	def processRuleForCompUnit(RuleEngineBlackboard blackboard, CompilationUnit unit) {
		val pcmDetector = blackboard.getPCMDetector()
		if (pcmDetector === null) {
			return
		}

		val identifier = new CompUnitOrName(unit)

		// technology based and general recognition
		val isConverter = isUnitAnnotatedWithName(unit, "Converter")
		if (isConverter) {
			detectDefault(blackboard, unit)
			return
		}

		// detect controller component	
		val isUnitController = isUnitAnnotatedWithName(unit, "Path")
		if (isUnitController) {
			var unitPath = getUnitAnnotationStringValue(unit, "Path")
			if (unitPath === null) {
				unitPath = ""
			}
			val path = "/" + unitPath
			pcmDetector.detectComponent(identifier)
			getMethods(unit).forEach [ m |
				if (isMethodAnnotatedWithName(m, "DELETE", "GET", "HEAD", "PUT", "POST", "OPTIONS")) {
					var methodPath = getMethodAnnotationStringValue(m, "Path")
					if (methodPath === null) {
						methodPath = path
					} else {
						methodPath = path + "/" + methodPath
					}
					methodPath = RESTHelper.replaceArgumentsWithWildcards(methodPath)
					// TODO: HTTP method switch-case
					pcmDetector.detectCompositeProvidedOperation(identifier, m.resolveBinding, new RESTName("TODO-host", methodPath, Optional.empty))
				}
			]
			// getFields(unit).forEach[f|if(isFieldAbstract(f)) pcmDetector.detectRequiredInterface(identifier, f)]
			return
		}

		val isWebServlet = isUnitAnnotatedWithName(unit, "WebServlet")
		if (isWebServlet) {
			// TODO: path
			pcmDetector.detectComponent(identifier)
			getMethods(unit).forEach [ m |
				if (isMethodModifiedExactlyWith(m, "public") || isMethodModifiedExactlyWith(m, "protected"))
					pcmDetector.detectProvidedOperation(identifier, m.resolveBinding)
			]
			// getFields(unit).forEach[f|if(isFieldAbstract(f)) pcmDetector.detectRequiredInterface(identifier, f)]
			return
		}
	}

	def detectDefault(RuleEngineBlackboard blackboard, CompilationUnit unit) {
		val pcmDetector = blackboard.getPCMDetector()
		val identifier = new CompUnitOrName(unit)

		pcmDetector.detectComponent(identifier)
		getAllPublicMethods(unit).forEach[m|pcmDetector.detectProvidedOperation(identifier, m.resolveBinding)]
		getFields(unit).forEach[f|if(isFieldAbstract(f)) pcmDetector.detectRequiredInterface(identifier, f)]
	}

	override isBuildRule() {
		return false
	}

	override getConfigurationKeys() {
		return Set.of
	}

	override getID() {
		return RULE_ID
	}

	override getName() {
		return "JAX RS Rules"
	}

	override getRequiredServices() {
		return Set.of(JAVA_DISCOVERER_ID)
	}

	override getDependentServices() {
		Set.of
	}
}
