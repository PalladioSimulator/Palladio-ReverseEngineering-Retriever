package org.palladiosimulator.retriever.extraction.rules

import java.nio.file.Path
import org.eclipse.jdt.core.dom.CompilationUnit
import static org.palladiosimulator.retriever.extraction.engine.RuleHelper.*
import org.palladiosimulator.retriever.extraction.commonalities.CompUnitOrName
import java.util.Set
import org.palladiosimulator.retriever.extraction.engine.Rule
import org.palladiosimulator.retriever.extraction.commonalities.RESTName
import java.util.Optional
import org.palladiosimulator.retriever.extraction.rules.util.RESTHelper
import java.util.Map
import org.palladiosimulator.retriever.extraction.commonalities.HTTPMethod
import org.palladiosimulator.retriever.extraction.blackboard.RetrieverBlackboard

class JaxRSRules implements Rule {

	public static final String RULE_ID = "org.palladiosimulator.retriever.extraction.rules.jax_rs"

	public static final String JAVA_DISCOVERER_ID = "org.palladiosimulator.retriever.extraction.discoverers.java"

	static final Map<String, HTTPMethod> SERVLET_METHODS = Map.of("doGet", HTTPMethod.GET, "doPost", HTTPMethod.POST,
		"doDelete", HTTPMethod.DELETE, "doPut", HTTPMethod.PUT, "handleGETRequest", HTTPMethod.GET, "handlePOSTRequest",
		HTTPMethod.POST, "handleDELETERequest", HTTPMethod.DELETE, "handlePUTRequest", HTTPMethod.PUT);

	override processRules(RetrieverBlackboard blackboard, Path path) {
		val unit = blackboard.getDiscoveredFiles(JAVA_DISCOVERER_ID, typeof(CompilationUnit)).get(path)

		if(unit === null) return;

		processRuleForCompUnit(blackboard, unit)
	}

	def processRuleForCompUnit(RetrieverBlackboard blackboard, CompilationUnit unit) {
		val pcmDetector = blackboard.getPCMDetector()
		if (pcmDetector === null) {
			return
		}

		val identifier = new CompUnitOrName(unit)
		val isConverter = isUnitAnnotatedWithName(unit, "Converter")
		val isUnitController = isUnitAnnotatedWithName(unit, "Path")
		val isWebServlet = isUnitAnnotatedWithName(unit, "WebServlet") || isImplementingOrExtending(unit, "HttpServlet")

		if(identifier.toString.endsWith("Test")) return;

		if(isAbstraction(unit)) return;

		// technology based and general recognition
		if (isConverter) {
			detectDefault(blackboard, unit)
		} // detect controller component	
		else if (isUnitController) {
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
					pcmDetector.detectCompositeProvidedOperation(identifier, m.resolveBinding,
						new RESTName("SERVICE-HOST", methodPath, Optional.empty))
				}
			]
			getFields(unit).forEach[f|pcmDetector.detectRequiredInterfaceWeakly(identifier, f)]
			pcmDetector.detectPartOfComposite(identifier, getUnitName(unit));
		} else if (isWebServlet) {
			var unitPath = getUnitAnnotationStringValue(unit, "WebServlet")
			if (unitPath === null) {
				unitPath = ""
			}
			val path = "/" + unitPath
			pcmDetector.detectComponent(identifier)
			getMethods(unit).forEach [ m |
				if (SERVLET_METHODS.containsKey(m.name.identifier)) {
					pcmDetector.detectProvidedOperation(identifier, m.resolveBinding,
						new RESTName("SERVICE-HOST", path, Optional.of(SERVLET_METHODS.get(m.name.identifier))))
				}
			]
			getFields(unit).forEach[f|pcmDetector.detectRequiredInterfaceWeakly(identifier, f)]
			pcmDetector.detectPartOfComposite(identifier, getUnitName(unit));
		} else {
			detectDefault(blackboard, unit)
		}

		for (parent : getAllAbstractParents(unit)) {
			System.out.println(identifier.name + ": " + parent)
			val parentBinding = parent.resolveBinding
			pcmDetector.detectProvidedInterfaceWeakly(identifier, parentBinding)
			for (m : getMethods(parent)) {
				System.out.println("\t " + m.name)
				pcmDetector.detectProvidedOperationWeakly(identifier, parentBinding, m)
			}
		}
	}

	def detectDefault(RetrieverBlackboard blackboard, CompilationUnit unit) {
		val pcmDetector = blackboard.getPCMDetector()
		val identifier = new CompUnitOrName(unit)

		pcmDetector.detectComponent(identifier)

		getAllPublicMethods(unit).forEach[m|pcmDetector.detectProvidedOperationWeakly(identifier, m.resolveBinding)]
		// Do not detect requirements of services, this may connect them too tightly
		if (!identifier.name.endsWith("ServiceImpl")) {
			getFields(unit).forEach[f|pcmDetector.detectRequiredInterfaceWeakly(identifier, f)]
		}
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
