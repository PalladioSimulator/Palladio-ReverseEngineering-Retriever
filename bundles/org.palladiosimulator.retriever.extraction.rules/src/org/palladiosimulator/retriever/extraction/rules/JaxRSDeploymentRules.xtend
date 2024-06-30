package org.palladiosimulator.retriever.extraction.rules

import java.nio.file.Path
import java.util.HashMap
import java.util.List
import java.util.Map
import java.util.Set
import org.jdom2.Document
import org.palladiosimulator.retriever.extraction.rules.data.GatewayRoute
import java.util.stream.Collectors
import org.palladiosimulator.retriever.extraction.rules.util.ProjectHelper
import java.util.ArrayList
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration
import org.palladiosimulator.retriever.services.blackboard.RetrieverBlackboard
import org.palladiosimulator.retriever.services.Rule

class JaxRSDeploymentRules implements Rule {
	public static final String RULE_ID = "org.palladiosimulator.retriever.extraction.rules.jax_rs.deployment"
	public static final String XML_DISCOVERER_ID = "org.palladiosimulator.retriever.extraction.discoverers.xml"
	public static final String JAVA_DISCOVERER_ID = "org.palladiosimulator.retriever.extraction.discoverers.java";
	public static final String ECMASCRIPT_RULE_ID = "org.palladiosimulator.retriever.extraction.rules.ecmascript"
	public static final String ECMASCRIPT_ROUTES_ID = "org.palladiosimulator.retriever.extraction.rules.ecmascript.routes"
	public static final String ECMASCRIPT_HOSTNAMES_ID = "org.palladiosimulator.retriever.extraction.rules.ecmascript.hostnames"

	override processRules(RetrieverBlackboard blackboard, Path path) {
		// TODO: run only once per project
		val xmls = blackboard.getDiscoveredFiles(XML_DISCOVERER_ID, typeof(Document))

		var Map<Path, String> hostnames = new HashMap();
		if (blackboard.hasPartition(RULE_ID)) {
			hostnames = blackboard.getPartition(RULE_ID) as Map<Path, String>
		}

		val List<GatewayRoute> routes = new ArrayList();

		for (xmlEntry : xmls.entrySet) {
			val xmlPath = xmlEntry.key
			val xml = xmlEntry.value
			if (xmlPath.endsWith("WEB-INF/web.xml")) {
				val servlets = xml.rootElement.getChildren("servlet", xml.rootElement.namespace).stream.collect(
					Collectors.toMap( [ servlet |
						val classTag = servlet.getChildTextTrim("servlet-class", servlet.namespace);
						if (classTag !== null) {
							return classTag
						} else {
							return servlet.getChildTextTrim("servlet-name", servlet.namespace)
						}
					], [ servlet |
						servlet.getChildTextTrim("servlet-name", servlet.namespace)
					])
				)

				val servletMappings = xml.rootElement.getChildren("servlet-mapping", xml.rootElement.namespace).stream.
					collect(
						Collectors.toMap( [ servlet |
							servlet.getChildTextTrim("servlet-name", servlet.namespace)
						], [servlet|servlet.getChildTextTrim("url-pattern", servlet.namespace)])
					)

				for (servletName : servlets.values) {
					routes.add(new GatewayRoute(servletMappings.get(servletName), servletName, true))
				}

				val nameToPath = new HashMap()
				val compilationUnits = blackboard.getDiscoveredFiles(JAVA_DISCOVERER_ID, CompilationUnit)
				for (entry : compilationUnits.entrySet) {
					val compilationUnitPath = entry.key
					val compilationUnit = entry.value
					val types = compilationUnit.types
					if (!types.empty) {
						val name = (types.get(0) as AbstractTypeDeclaration).resolveBinding.qualifiedName
						nameToPath.put(name, compilationUnitPath)
					}
				}
				for (servlet : servlets.entrySet) {
					val servletPath = nameToPath.get(servlet.key)
					hostnames.put(servletPath, servlet.value)
				}
			}
		}

		if (!blackboard.hasPartition(RULE_ID)) {
			blackboard.addPartition(RULE_ID, hostnames)
		}

		val projectRoot = ProjectHelper.findProjectRoot(path, "pom.xml", "build.gradle")

		if (blackboard.hasPartition(ECMASCRIPT_ROUTES_ID)) {
			val ecmaScriptRouteMap = blackboard.getPartition(ECMASCRIPT_ROUTES_ID) as Map<Path, List<GatewayRoute>>
			if (ecmaScriptRouteMap.containsKey(projectRoot)) {
				ecmaScriptRouteMap.get(projectRoot).addAll(routes)
			} else {
				ecmaScriptRouteMap.put(projectRoot, routes)
			}
		} else {
			val routeMap = new HashMap()
			routeMap.put(projectRoot, routes)
			blackboard.addPartition(ECMASCRIPT_ROUTES_ID, routeMap)
		}

		var Map<Path, String> hostnameMap = new HashMap<Path, String>()
		if (blackboard.hasPartition(ECMASCRIPT_HOSTNAMES_ID)) {
			hostnameMap = blackboard.getPartition(ECMASCRIPT_HOSTNAMES_ID) as Map<Path, String>
		}
		hostnameMap.putAll(hostnames)
		if (!blackboard.hasPartition(ECMASCRIPT_HOSTNAMES_ID)) {
			blackboard.addPartition(ECMASCRIPT_HOSTNAMES_ID, hostnameMap)
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
		return "JAX RS Deployment Rules"
	}

	override getRequiredServices() {
		return Set.of(XML_DISCOVERER_ID, JAVA_DISCOVERER_ID)
	}

	override getDependentServices() {
		Set.of(ECMASCRIPT_RULE_ID)
	}
}
