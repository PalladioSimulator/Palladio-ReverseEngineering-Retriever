package org.palladiosimulator.somox.analyzer.rules.impl

import java.nio.file.Path
import java.util.ArrayList
import java.util.HashMap
import java.util.List
import java.util.Map
import java.util.Optional
import java.util.Properties
import java.util.Set
import java.util.function.Function
import org.apache.log4j.Logger
import org.jdom2.Document
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard
import org.palladiosimulator.somox.analyzer.rules.engine.Rule
import org.palladiosimulator.somox.analyzer.rules.impl.util.SpringHelper
import org.palladiosimulator.somox.analyzer.rules.impl.data.SpringZuulRoute

class SpringZuulRules implements Rule {
	static final Logger LOG = Logger.getLogger(SpringZuulRules)

	public static final String RULE_ID = "org.palladiosimulator.somox.analyzer.rules.impl.spring.zuul"
	public static final String YAML_DISCOVERER_ID = "org.palladiosimulator.somox.discoverer.yaml"
	public static final String YAML_MAPPERS_KEY = YAML_DISCOVERER_ID + ".mappers"
	public static final String XML_DISCOVERER_ID = "org.palladiosimulator.somox.discoverer.xml"
	public static final String PROPERTIES_DISCOVERER_ID = "org.palladiosimulator.somox.discoverer.properties"
	public static final String ECMASCRIPT_RULE_ID = "org.palladiosimulator.somox.analyzer.rules.impl.ecmascript"

	override processRules(RuleEngineBlackboard blackboard, Path path) {
		val rawYamls = blackboard.getPartition(YAML_DISCOVERER_ID) as Map<Path, Iterable<Map<String, Object>>>
		val yamlMappers = blackboard.getPartition(YAML_MAPPERS_KEY) as Map<Path, Function<String, Optional<String>>>
		val poms = blackboard.getDiscoveredFiles(XML_DISCOVERER_ID, typeof(Document))
		val propertyFiles = blackboard.getDiscoveredFiles(PROPERTIES_DISCOVERER_ID, typeof(Properties))

		val projectRoot = SpringHelper.findProjectRoot(path, poms)
		val configRoot = SpringHelper.findConfigRoot(poms)

		var routeMap = blackboard.getPartition(RULE_ID) as Map<Path, List<SpringZuulRoute>>
		if (routeMap === null) {
			routeMap = new HashMap<Path, List<SpringZuulRoute>>()
		}

		// Execute only once for each Spring application/service
		if(routeMap.containsKey(projectRoot)) return

		val bootstrapYaml = yamlMappers.get(
			SpringHelper.findFile(yamlMappers.keySet, projectRoot.resolve("src/main/resources"),
				Set.of("bootstrap.yaml", "bootstrap.yml")))
		val applicationProperties = propertyFiles.get(
			SpringHelper.findFile(propertyFiles.keySet, projectRoot.resolve("src/main/resources"),
				Set.of("application.properties")))
		val applicationName = SpringHelper.getFromYamlOrProperties("spring.application.name", bootstrapYaml,
			applicationProperties)
		val projectConfigYaml = rawYamls.get(
			SpringHelper.findFile(rawYamls.keySet, configRoot.resolve("src/main/resources/shared"),
				Set.of(applicationName + ".yaml", applicationName + ".yml")))

		// Query zuul.routes in config server only (for now)
		val routes = collectRoutes(projectConfigYaml)
		for (route : routes) {
			LOG.warn("Route in " + applicationName + ": " + route.path + " -> " + route.serviceId)
		}
		routeMap.put(projectRoot, routes)
		blackboard.addPartition(RULE_ID, routeMap)
	}

	def List<SpringZuulRoute> collectRoutes(Iterable<Map<String, Object>> applicationYamlIter) {
		val result = new ArrayList()

		if(applicationYamlIter === null || applicationYamlIter.empty) return result

		val applicationYaml = applicationYamlIter.get(0) as Map<String, Object>
		if(applicationYaml === null) return result

		val zuulObject = applicationYaml.get("zuul")
		if(!(zuulObject instanceof Map)) return result
		val zuul = zuulObject as Map<String, Object>

		val routesObject = zuul.get("routes")
		if(!(routesObject instanceof Map)) return result
		val routes = routesObject as Map<String, Map<String, Object>>

		for (route : routes.values) {
			val pathObject = route.get("path")
			val serviceIdObject = route.get("serviceId")
			if (pathObject !== null && serviceIdObject !== null && pathObject instanceof String &&
				serviceIdObject instanceof String) {
				val path = pathObject as String
				val serviceId = serviceIdObject as String
				result.add(new SpringZuulRoute(path, serviceId))
			}
		}

		return result;
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
		return "Spring Zuul Rules"
	}

	override getRequiredServices() {
		return Set.of(YAML_DISCOVERER_ID, XML_DISCOVERER_ID, PROPERTIES_DISCOVERER_ID)
	}

	override getDependentServices() {
		Set.of(ECMASCRIPT_RULE_ID)
	}
}
