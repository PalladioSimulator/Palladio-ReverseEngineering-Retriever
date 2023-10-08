package org.palladiosimulator.somox.analyzer.rules.impl

import org.palladiosimulator.somox.analyzer.rules.engine.IRule

import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard
import java.nio.file.Path;
import org.eclipse.jdt.core.dom.CompilationUnit
import static org.palladiosimulator.somox.analyzer.rules.engine.RuleHelper.*
import org.palladiosimulator.somox.analyzer.rules.model.CompUnitOrName

class JaxRSRules extends IRule{

    public static final String JAVA_DISCOVERER_ID = "org.palladiosimulator.somox.discoverer.java"
	
	new(RuleEngineBlackboard blackboard) {
		super(blackboard)
	}
	
	override boolean processRules(Path path) {
		val unit = blackboard.getDiscoveredFiles(JAVA_DISCOVERER_ID, typeof(CompilationUnit)).get(path)
		
		if (unit === null) return false;
		
		return processRuleForCompUnit(unit)
	}
	
	def boolean processRuleForCompUnit(CompilationUnit unit) {
		val pcmDetector = blackboard.getPCMDetector()
		if (pcmDetector === null) {
		return false
		}
		
		val identifier = new CompUnitOrName(unit)

		// technology based and general recognition
		val isConverter = isUnitAnnotatedWithName(unit, "Converter")
		if(isConverter){
			detectDefault(unit)
		return true
		}
		
		// detect controller component	
		val isUnitController = isUnitAnnotatedWithName(unit, "Path")
		if(isUnitController){
			pcmDetector.detectComponent(identifier) 
			getMethods(unit).forEach[m|
			if(isMethodAnnotatedWithName(m,"DELETE","GET","HEAD","PUT","POST","OPTIONS")) pcmDetector.detectProvidedOperation(identifier,m.resolveBinding)]
			getFields(unit).forEach[f|if(isFieldAbstract(f)) pcmDetector.detectRequiredInterface(identifier, f)]
		return true
		} 
		
		val isWebListener = isUnitAnnotatedWithName(unit, "WebListener","WebServlet")
		if(isWebListener){
			pcmDetector.detectComponent(identifier)
			getMethods(unit).forEach[m|
			if(isMethodModifiedExactlyWith(m,"public") || isMethodModifiedExactlyWith(m,"protected")) pcmDetector.detectProvidedOperation(identifier,m.resolveBinding)]
			getFields(unit).forEach[f|if(isFieldAbstract(f)) pcmDetector.detectRequiredInterface(identifier, f)]
		return true
		}
		
		// detect implementing component
		val isUnit = isClassImplementing(unit)
		if(isUnit && !isUnitController && !isWebListener && getAllInterfaces(unit).size() > 0){
			pcmDetector.detectComponent(identifier)
			val firstIn = getAllInterfaces(unit).get(0)
			getMethods(firstIn).forEach[m|pcmDetector.detectProvidedOperation(identifier, firstIn.resolveBinding, m)]
			getFields(unit).forEach[f|if(isFieldAbstract(f)) pcmDetector.detectRequiredInterface(identifier, f)]
			return true
		}
		
		// detect normal components
		val classModified = isClassModifiedExactlyWith(unit, "public","final");
		if(!isUnit && !isUnitController && !isWebListener && classModified){
			pcmDetector.detectComponent(identifier)
			detectDefault(unit)
			return true
		} 
		return false
		
	}
	
	def detectDefault(CompilationUnit unit) {
		val pcmDetector = blackboard.getPCMDetector()
		val identifier = new CompUnitOrName(unit)

		pcmDetector.detectComponent(identifier)
		getAllPublicMethods(unit).forEach[m|pcmDetector.detectProvidedOperation(identifier,m.resolveBinding)]
		getFields(unit).forEach[f|if(isFieldAbstract(f)) pcmDetector.detectRequiredInterface(identifier, f)]
	}
	
}
