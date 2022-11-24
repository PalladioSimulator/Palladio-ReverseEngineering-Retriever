package org.palladiosimulator.somox.analyzer.rules.impl.eclipse

import static org.palladiosimulator.somox.analyzer.rules.engine.EclipseRuleHelper.*
import org.palladiosimulator.somox.analyzer.rules.engine.IRule
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard
import java.nio.file.Path
import org.eclipse.jdt.core.dom.CompilationUnit

class PiggymetricsRules extends IRule {
	new(RuleEngineBlackboard blackboard) {
		super(blackboard)
	}

	override boolean processRules(Path path) {
		val units = blackboard.getCompilationUnitAt(path)

		var containedSuccessful = false
		for (unit : units) {
			if (unit.isEclipseCompilationUnit) {
				val eclipseUnit = unit.getEclipseCompilationUnit
				containedSuccessful = processRuleForCompUnit(eclipseUnit) || containedSuccessful
			}
		}

		return containedSuccessful
	}

	def boolean processRuleForCompUnit(CompilationUnit unit) {
		val pcmDetector = blackboard.eclipsePCMDetector
		if (pcmDetector === null) {
			return false
		}

		// Abort if there is no CompilationUnit at the specified path
		if (unit === null) {
			return false
		}

		val isService = isUnitAnnotatedWithName(unit, "Service")
		val isController = isUnitAnnotatedWithName(unit, "RestController")
		val isClient = isUnitAnnotatedWithName(unit, "FeignClient")
		val isComponent = isService || isController || isClient

		if (isComponent) {
			pcmDetector.detectComponent(unit)
		}
		
		if (isService || isController) {
			for (f : getFields(unit)) {
				val annotated = isFieldAnnotatedWithName(f, "Autowired")
				if (annotated) {
					pcmDetector.detectRequiredInterface(unit, f)
				}
			}
		}
		
		if (isService) {
			pcmDetector.detectPartOfComposite(unit, getUnitName(unit));
		}

		if (isController) {
			val requestedMapping = getUnitAnnotationStringValue(unit, "RequestMapping");
			if (requestedMapping !== null) {
				val ifaceName = requestedMapping.substring(requestedMapping.lastIndexOf('/') + 1);
				pcmDetector.detectOperationInterface(unit, ifaceName);
				for (m : getMethods(unit)) {
					val annotated = isMethodAnnotatedWithName(m, "RequestMapping");
					if (annotated) {
						pcmDetector.detectCompositeProvidedOperation(unit, ifaceName, m.resolveBinding);
					}
				}
			}
		}

		if (isClient) {
			for (m : getMethods(unit)) {
				val annotated = isMethodAnnotatedWithName(m, "RequestMapping");
				if (annotated) {
					val requestedMapping = getMethodAnnotationStringValue(m, "RequestMapping");
					var ifaceName = "";
					val argumentIndex = requestedMapping.indexOf('{');
					if (argumentIndex >= 0) {
						val lastSegmentStart = requestedMapping.lastIndexOf('/', argumentIndex)
						val secondToLastSegmentStart = requestedMapping.lastIndexOf('/', lastSegmentStart - 1);
						ifaceName = requestedMapping.substring(secondToLastSegmentStart + 1, lastSegmentStart);
					} else {
						ifaceName = requestedMapping.substring(requestedMapping.lastIndexOf('/') + 1);
					}
					pcmDetector.detectCompositeRequiredInterface(unit, ifaceName);
				}
			}
		}

		var inFs = getAllInterfaces(unit)
		val isImplementingOne = inFs.size == 1

		if (isComponent && isImplementingOne) {
			var firstIn = inFs.get(0)
			pcmDetector.detectOperationInterface(firstIn)
			for (m : getMethods(firstIn)) {
				pcmDetector.detectProvidedOperation(unit, firstIn.resolveBinding, m)
			}
		}

		return true;
	}
}
