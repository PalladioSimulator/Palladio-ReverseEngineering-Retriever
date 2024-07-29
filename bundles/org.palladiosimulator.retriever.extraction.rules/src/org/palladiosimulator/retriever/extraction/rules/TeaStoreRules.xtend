package org.palladiosimulator.retriever.extraction.rules

import java.nio.file.Path;
import java.util.HashSet
import org.eclipse.jdt.core.dom.CompilationUnit
import java.util.Set
import org.palladiosimulator.retriever.services.blackboard.RetrieverBlackboard
import org.palladiosimulator.retriever.services.Rule
import org.palladiosimulator.retriever.extraction.engine.PCMDetector
import org.palladiosimulator.retriever.extraction.commonalities.CompUnitOrName
import java.util.Map
import static org.palladiosimulator.retriever.extraction.engine.RuleHelper.*
import org.eclipse.jdt.core.dom.ASTVisitor
import org.eclipse.jdt.core.dom.ExpressionStatement
import org.eclipse.jdt.core.dom.MethodInvocation
import org.palladiosimulator.retriever.extraction.commonalities.RESTOperationName
import org.palladiosimulator.retriever.extraction.commonalities.RESTName

class TeaStoreRules implements Rule {

	static final String RULE_ID = "org.palladiosimulator.retriever.extraction.rules.teastore"
	static final String JAVA_DISCOVERER_ID = "org.palladiosimulator.retriever.extraction.discoverers.java";
	static final String DOCKER_DISCOVERER_ID = "org.palladiosimulator.retriever.extraction.discoverers.docker";
	static final String JAX_RS_RULE_ID = "org.palladiosimulator.retriever.extraction.rules.jax_rs";
	static final String JAX_RS_DEPLOYMENT_RULE_ID = "org.palladiosimulator.retriever.extraction.rules.jax_rs.deployment";
	static final String DOCKER_FILE_NAME = "Dockerfile";

	override processRules(RetrieverBlackboard blackboard, Path path) {
		if (path === null) {
			return
		}
		
		val compilationUnits = blackboard.getDiscoveredFiles(JAVA_DISCOVERER_ID, typeof(CompilationUnit));
		val pcmDetector = blackboard.PCMDetector as PCMDetector;

		if (path.fileName.toString().equals(DOCKER_FILE_NAME)) {
			val parentName = path.parent.fileName.toString()

			// Add all file system children as associated compilation units
			var children = new HashSet<CompilationUnit>();
			var parentPath = path.parent;
			for (entry : compilationUnits.entrySet) {
				if (entry.key.startsWith(parentPath)) {
					children.add(entry.value);
				}
			}
			for (unit : children) {
				pcmDetector.detectSeparatingIdentifier(new CompUnitOrName(unit), parentName)
				pcmDetector.detectPartOfWeakComposite(new CompUnitOrName(unit), parentName)
			}

			val hostnameMap = blackboard.getPartition(JAX_RS_DEPLOYMENT_RULE_ID) as Map<Path, String>
			var hasHostnamePath = false
			for (hostnamePath : hostnameMap.keySet) {
				if (hostnamePath !== null && path.startsWith(hostnamePath)) {
					hasHostnamePath = true
				}
			}
			if (!hasHostnamePath) {
				hostnameMap.put(path.parent, parentName)
			}
		}
		
		if (compilationUnits.containsKey(path)) {
			val unit = compilationUnits.get(path)
			val identifier = new CompUnitOrName(unit)
			if (identifier.name().endsWith("Startup") && !identifier.name.endsWith("LogReaderStartup")) {
				// Make Startup components part of a composite so that they become part of the weak Dockerfile composite
				pcmDetector.detectPartOfComposite(identifier, identifier.name() + "_tempComposite")
				val contextInitializedMethod = getMethods(unit).stream().filter[m|m.name.fullyQualifiedName.endsWith("contextInitialized")].findFirst()
				if (contextInitializedMethod.present) {
					contextInitializedMethod.get.accept(new TeaStoreASTVisitor(identifier, pcmDetector));
				}
			} else if (identifier.name().endsWith("ServiceLoadBalancer")) {
				pcmDetector.detectComponent(identifier)
			} else if (identifier.name().endsWith("RegistryClient")) {
				pcmDetector.detectComponent(identifier)
				pcmDetector.detectRequiredInterface(identifier, new RESTName("tools.descartes.teastore.registry", "/services"))
			}
		}
	}
	
	static class TeaStoreASTVisitor extends ASTVisitor {
		CompUnitOrName identifier;
		PCMDetector pcmDetector;
		
		new(CompUnitOrName identifier, PCMDetector pcmDetector) {
			this.identifier = identifier;
			this.pcmDetector = pcmDetector
		}
		
		override visit(ExpressionStatement statement) {
			if (!(statement.expression instanceof MethodInvocation)) {
				return true
			}
			
			val invocation = statement.expression as MethodInvocation
			// TODO: avoid self-references
			pcmDetector.detectCompositeRequiredInterfaceWeakly(identifier, invocation)

			return true
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
		return "TeaStore Rules"
	}

	override getRequiredServices() {
		return Set.of(JAVA_DISCOVERER_ID, DOCKER_DISCOVERER_ID, JAX_RS_DEPLOYMENT_RULE_ID)
	}

	override getDependentServices() {
		Set.of(JAX_RS_RULE_ID)
	}
}
