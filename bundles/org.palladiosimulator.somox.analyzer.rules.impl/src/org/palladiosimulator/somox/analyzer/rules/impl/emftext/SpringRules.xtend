package org.palladiosimulator.somox.analyzer.rules.impl.emftext

import org.palladiosimulator.somox.analyzer.rules.engine.IRule

import static org.palladiosimulator.somox.analyzer.rules.engine.EMFTextRuleHelper.*
import org.emftext.language.java.containers.impl.CompilationUnitImpl
import org.emftext.language.java.members.Method
import org.emftext.language.java.members.Field
import org.emftext.language.java.parameters.Parameter
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard
import java.nio.file.Path;

class SpringRules extends IRule {
	
	new(RuleEngineBlackboard blackboard) {
		super(blackboard)
	}
	
	override boolean processRules(Path path) {
		val units = blackboard.getCompilationUnitAt(path)
	
		var containedSuccessful = false
		for (unit : units) {
			if (unit.isEMFTextCompilationUnit) {
				val emfUnit = unit.getEMFTextCompilationUnit()
				containedSuccessful = processRuleForCompUnit(emfUnit) || containedSuccessful
			}
		}
		
		return containedSuccessful
	}
	
	def boolean processRuleForCompUnit(CompilationUnitImpl unit) {
		val pcmDetector = blackboard.getEMFTextPCMDetector()
		if (pcmDetector === null) {
			return false
		}
		
		// Abort if there is no CompilationUnit at the specified path
		if (unit === null) {
			return false
		}
		
		val isAbstract = isAbstraction(unit)
		
		// Component detection
		val isComponent = !isAbstract && isUnitAnnotatedWithName(unit, "Component","Service","Controller","RestController","RequestMapping","ControllerAdvice")
		
		if(isComponent) pcmDetector.detectComponent(unit)
		
		// Component Detection for Spring Repository
		if((isUnitAnnotatedWithName(unit,"FeignClient","Repository") || (isUnitNamedWith(unit, "Repository")) && isAbstract)) {
			pcmDetector.detectComponent(unit) 
			pcmDetector.detectOperationInterface(unit)
			getMethods(unit).forEach[m|pcmDetector.detectProvidedInterface(unit, m)]
		}
		
		// Operation Interface Detection
		// if implementing 1 interface
		var inFs = getAllInterfaces(unit)
		val isementingOne = inFs.size==1
		if(isComponent && isementingOne) {
			var firstIn = inFs.get(0)
			pcmDetector.detectOperationInterface(firstIn)
			for(Method m: getMethods(firstIn)){
				pcmDetector.detectProvidedInterface(unit, firstIn, m)
			}
		}
			
		// not implementing 1 interface => Controller class with annotations on methods  
		if(isComponent && !isementingOne) 
			for(Method m: getMethods(unit)){
				val annoWithName = isMethodAnnotatedWithName(m,"RequestMapping","GetMapping","PutMapping","PostMapping","DeleteMapping","PatchMapping")

				if(annoWithName || (!annoWithName && m.public)) 
					pcmDetector.detectProvidedInterface(unit, m) pcmDetector.detectOperationInterface(unit)
			}
				
		
		// Required Role Detection
		if(isComponent){
			
			// field injection
			for(Field f: getFields(unit)){
				val annotated = isFieldAnnotatedWithName(f, "Autowired")
				if(annotated){
					pcmDetector.detectRequiredInterface(unit, f)
				}
				val abstr = isFieldAbstract(f)
				val modi = isFieldModifiedExactlyWith(f, "private","final")
				if(!annotated && abstr && modi){
					pcmDetector.detectRequiredInterface(unit, f)
				}
				// if class of field is annotated
				if(!abstr && modi && isClassOfFieldAnnotatedWithName(f,"Component","Service","Controller","RestController","RequestMapping","ControllerAdvice")){
					pcmDetector.detectRequiredInterface(unit, f)
				}
				
			}
			
			// setter injection
			for(Method m: getMethods(unit)){
				if(isMethodAnnotatedWithName(m, "Autowired")){
					for(Parameter p: m.parameters){
						// if abstract type
						val isParaAbstract = isParameterAbstract(p)
						if(isParaAbstract){
							pcmDetector.detectRequiredInterface(unit, p)
						}
						// if type is component
						if(!isParaAbstract && isParameterAClassAnnotatedWith(p,"Component","Service","Controller","RestController","RequestMapping","ControllerAdvice")){
							pcmDetector.detectRequiredInterface(unit, p)
						}
					}
				}
			}
			
			// constructor injection
			getConstructors(unit).forEach[constructor | {
				if(isConstructorAnnotatedWithName(constructor,"Autowired")){
					constructor.parameters.forEach[para | {
					if(isParameterAbstract(para) || isParameterAClassAnnotatedWith(para,"Component","Service","Controller","RestController","RequestMapping","ControllerAdvice")){
						pcmDetector.detectRequiredInterface(unit, para)
					}
					if(!isParameterAbstract(para) && isParameterAnnotatedWith(para,"LoadBalanced")){
						pcmDetector.detectRequiredInterface(unit, para)
					} 
				}]
				}
			}];
		}
		return true;
	}
}
