package org.palladiosimulator.somox.analyzer.rules.impl.jamopp

import org.emftext.language.java.containers.impl.CompilationUnitImpl
import org.palladiosimulator.somox.analyzer.rules.engine.IRule

import static org.palladiosimulator.somox.analyzer.rules.engine.RuleHelper.*
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard
import java.nio.file.Path;

class JaxRSRules extends IRule{
	
	new(RuleEngineBlackboard blackboard) {
		super(blackboard)
	}
	
	override boolean processRules(Path path) {
		val unitImpls = blackboard.getCompilationUnitAt(path)
		
		var containedSuccessful = false
		for (unitImpl : unitImpls) {
			containedSuccessful = processRuleForCompUnit(unitImpl) || containedSuccessful
		}
		
		return containedSuccessful
	}
	
	def boolean processRuleForCompUnit(CompilationUnitImpl unitImpl) {
		val pcmDetector = blackboard.getPCMDetector()

		// technology based and general recognition
		val isConverter = isUnitAnnotatedWithName(unitImpl, "Converter")
		if(isConverter){
			detectDefault(unitImpl)
		return true
		}
		
		// detect controller component	
		val isUnitController = isUnitAnnotatedWithName(unitImpl, "Path")
		if(isUnitController){
			pcmDetector.detectComponent(unitImpl) 
			pcmDetector.detectOperationInterface(unitImpl)
			getMethods(unitImpl).forEach[m|
			if(isMethodAnnotatedWithName(m,"DELETE","GET","HEAD","PUT","POST","OPTIONS")) pcmDetector.detectProvidedInterface(unitImpl,m)]
			getFields(unitImpl).forEach[f|if(isFieldAbstract(f)) pcmDetector.detectRequiredInterface(unitImpl, f)]
		return true
		} 
		
		val isWebListener = isUnitAnnotatedWithName(unitImpl, "WebListener","WebServlet")
		if(isWebListener){
			pcmDetector.detectComponent(unitImpl)
			pcmDetector.detectOperationInterface(unitImpl)
			getMethods(unitImpl).forEach[m|
			if(isMethodModifiedExactlyWith(m,"public") || isMethodModifiedExactlyWith(m,"protected")) pcmDetector.detectProvidedInterface(unitImpl,m)]
			getFields(unitImpl).forEach[f|if(isFieldAbstract(f)) pcmDetector.detectRequiredInterface(unitImpl, f)]
		return true
		}
		
		// detect implementing component
		val isUnitImpl = isClassImplementing(unitImpl)
		if(isUnitImpl && !isUnitController && !isWebListener && getAllInterfaces(unitImpl).size() > 0){
			pcmDetector.detectComponent(unitImpl)
			val firstIn = getAllInterfaces(unitImpl).get(0)
			pcmDetector.detectOperationInterface(firstIn)
			getMethods(firstIn).forEach[m|pcmDetector.detectProvidedInterface(unitImpl, firstIn, m)]
			getFields(unitImpl).forEach[f|if(isFieldAbstract(f)) pcmDetector.detectRequiredInterface(unitImpl, f)]
			return true
		}
		
		// detect normal components
		val classModified = isClassModifiedExactlyWith(unitImpl, "public","final");
		if(!isUnitImpl && !isUnitController && !isWebListener && classModified){
			pcmDetector.detectComponent(unitImpl)
			detectDefault(unitImpl)
			return true
		} 
		return false
		
	}
	
	def detectDefault(CompilationUnitImpl unitImpl) {
		val pcmDetector = blackboard.getPCMDetector()

		pcmDetector.detectComponent(unitImpl)
		pcmDetector.detectOperationInterface(unitImpl)
		getAllPublicMethods(unitImpl).forEach[m|pcmDetector.detectProvidedInterface(unitImpl,m)]
		getFields(unitImpl).forEach[f|if(isFieldAbstract(f)) pcmDetector.detectRequiredInterface(unitImpl, f)]
	}
	
}
