package org.palladiosimulator.somox.analyzer.rules.impl.eclipse

import org.palladiosimulator.somox.analyzer.rules.engine.IRule

import static org.palladiosimulator.somox.analyzer.rules.engine.EclipseRuleHelper.*
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard
import java.nio.file.Path;
import org.eclipse.jdt.core.dom.MethodDeclaration
import org.eclipse.jdt.core.dom.SingleVariableDeclaration
import java.util.List
import org.eclipse.jdt.core.dom.FieldDeclaration
import org.eclipse.jdt.core.dom.IMethodBinding

class SpringRules extends IRule {
	
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
		val pcmDetector = blackboard.getEclipsePCMDetector()
		
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
			getMethods(unit).forEach[m|pcmDetector.detectProvidedInterface(unit, m.resolveBinding)]
		}
		
		// Operation Interface Detection
		// if implementing 1 interface
		var inFs = getAllInterfaces(unit)
		val isementingOne = inFs.size==1
		if(isComponent && isementingOne) {
			var firstIn = inFs.get(0)
			pcmDetector.detectOperationInterface(firstIn)
			for(IMethodBinding m: getMethods(firstIn)){
				pcmDetector.detectProvidedInterface(unit, firstIn.resolveBinding, m)
			}
		}
			
		// not implementing 1 interface => Controller class with annotations on methods  
		val annoNames = List.of("RequestMapping","GetMapping","PutMapping","PostMapping","DeleteMapping","PatchMapping")
		if(isComponent && !isementingOne) 
			for(MethodDeclaration m: getMethods(unit)){
				val annoWithName = isMethodAnnotatedWithName(m, annoNames)

				if(annoWithName) 
					pcmDetector.detectProvidedInterface(unit, m.resolveBinding) pcmDetector.detectOperationInterface(unit)
			}
			
			for(MethodDeclaration m: getAllPublicMethods(unit)) {
				val annoWithName = isMethodAnnotatedWithName(m, annoNames)
				if(!annoWithName) 
					pcmDetector.detectProvidedInterface(unit, m.resolveBinding) pcmDetector.detectOperationInterface(unit)
			}
				
		
		// Required Role Detection
		if(isComponent){
			
			// field injection
			for(FieldDeclaration f: getFields(unit)){
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
			for(MethodDeclaration m: getMethods(unit)){
				if(isMethodAnnotatedWithName(m, "Autowired")){
					for(SingleVariableDeclaration p: m.parameters as List<SingleVariableDeclaration>){
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
