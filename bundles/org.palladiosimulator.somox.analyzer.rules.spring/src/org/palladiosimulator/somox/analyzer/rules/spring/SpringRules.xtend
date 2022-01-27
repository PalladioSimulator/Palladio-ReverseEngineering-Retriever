package org.palladiosimulator.somox.analyzer.rules.spring

import org.palladiosimulator.somox.analyzer.rules.engine.IRule

import static org.palladiosimulator.somox.analyzer.rules.engine.RuleHelper.*
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
		val unitImpls = blackboard.getCompilationUnitAt(path)
	
		var containedSuccessful = false
		for (unitImpl : unitImpls) {
			containedSuccessful = processRuleForCompUnit(unitImpl) || containedSuccessful
		}
		
		return containedSuccessful
	}
	
	def boolean processRuleForCompUnit(CompilationUnitImpl unitImpl) {
		val pcmDetector = blackboard.getPCMDetector()
		
		// Abort if there is no CompilationUnit at the specified path
		if (unitImpl === null) {
			return false
		}
		
		val isAbstract = isAbstraction(unitImpl)
		
		// Component detection
		val isComponent = !isAbstract && isUnitAnnotatedWithName(unitImpl, "Component","Service","Controller","RestController","RequestMapping","ControllerAdvice")
		
		if(isComponent) pcmDetector.detectComponent(unitImpl)
		
		// Component Detection for Spring Repository
		if((isUnitAnnotatedWithName(unitImpl,"FeignClient","Repository") || (isUnitNamedWith(unitImpl, "Repository")) && isAbstract)) {
			pcmDetector.detectComponent(unitImpl) 
			pcmDetector.detectOperationInterface(unitImpl)
			getMethods(unitImpl).forEach[m|pcmDetector.detectProvidedInterface(unitImpl, m)]
		}
		
		// Operation Interface Detection
		// if implementing 1 interface
		var inFs = getAllInterfaces(unitImpl)
		val isImplementingOne = inFs.size==1
		if(isComponent && isImplementingOne) {
			var firstIn = inFs.get(0)
			pcmDetector.detectOperationInterface(firstIn)
			for(Method m: getMethods(firstIn)){
				pcmDetector.detectProvidedInterface(unitImpl, firstIn, m)
			}
		}
			
		// not implementing 1 interface => Controller class with annotations on methods  
		if(isComponent && !isImplementingOne) 
			for(Method m: getMethods(unitImpl)){
				val annoWithName = isMethodAnnotatedWithName(m,"RequestMapping","GetMapping","PutMapping","PostMapping","DeleteMapping","PatchMapping")

				if(annoWithName || (!annoWithName && m.public)) 
					pcmDetector.detectProvidedInterface(unitImpl, m) pcmDetector.detectOperationInterface(unitImpl)
			}
				
		
		// Required Role Detection
		if(isComponent){
			
			// field injection
			for(Field f: getFields(unitImpl)){
				val annotated = isFieldAnnotatedWithName(f, "Autowired")
				if(annotated){
					pcmDetector.detectRequiredInterface(unitImpl, f)
				}
				val abstr = isFieldAbstract(f)
				val modi = isFieldModifiedExactlyWith(f, "private","final")
				if(!annotated && abstr && modi){
					pcmDetector.detectRequiredInterface(unitImpl, f)
				}
				// if class of field is annotated
				if(!abstr && modi && isClassOfFieldAnnotatedWithName(f,"Component","Service","Controller","RestController","RequestMapping","ControllerAdvice")){
					pcmDetector.detectRequiredInterface(unitImpl, f)
				}
				
			}
			
			// setter injection
			for(Method m: getMethods(unitImpl)){
				if(isMethodAnnotatedWithName(m, "Autowired")){
					for(Parameter p: m.parameters){
						// if abstract type
						val isParaAbstract = isParameterAbstract(p)
						if(isParaAbstract){
							pcmDetector.detectRequiredInterface(unitImpl, p)
						}
						// if type is component
						if(!isParaAbstract && isParameterAClassAnnotatedWith(p,"Component","Service","Controller","RestController","RequestMapping","ControllerAdvice")){
							pcmDetector.detectRequiredInterface(unitImpl, p)
						}
					}
				}
			}
			
			// constructor injection
			getConstructors(unitImpl).forEach[constructor | {
				if(isConstructorAnnotatedWithName(constructor,"Autowired")){
					constructor.parameters.forEach[para | {
					if(isParameterAbstract(para) || isParameterAClassAnnotatedWith(para,"Component","Service","Controller","RestController","RequestMapping","ControllerAdvice")){
						pcmDetector.detectRequiredInterface(unitImpl, para)
					}
					if(!isParameterAbstract(para) && isParameterAnnotatedWith(para,"LoadBalanced")){
						pcmDetector.detectRequiredInterface(unitImpl, para)
					} 
				}]
				}
			}];
		}
		return true;
	}
}
