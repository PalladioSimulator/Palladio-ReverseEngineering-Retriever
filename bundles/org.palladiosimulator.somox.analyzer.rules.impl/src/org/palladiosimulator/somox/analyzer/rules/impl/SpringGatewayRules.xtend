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
import org.palladiosimulator.somox.analyzer.rules.impl.data.GatewayRoute

class SpringGatewayRules implements Rule {
	static final Logger LOG = Logger.getLogger(SpringGatewayRules)

	public static final String RULE_ID = "org.palladiosimulator.somox.analyzer.rules.impl.spring.cloudgateway"
	public static final String YAML_DISCOVERER_ID = "org.palladiosimulator.somox.discoverer.yaml"
	public static final String YAML_MAPPERS_KEY = YAML_DISCOVERER_ID + ".mappers"
	public static final String XML_DISCOVERER_ID = "org.palladiosimulator.somox.discoverer.xml"
	public static final String PROPERTIES_DISCOVERER_ID = "org.palladiosimulator.somox.discoverer.properties"
	public static final String ECMASCRIPT_RULE_ID = "org.palladiosimulator.somox.analyzer.rules.impl.ecmascript"
	public static final String ECMASCRIPT_ROUTES_ID = "org.palladiosimulator.somox.analyzer.rules.impl.ecmascript.routes"
	public static final String ECMASCRIPT_HOSTNAMES_ID = "org.palladiosimulator.somox.analyzer.rules.impl.ecmascript.hostnames"

	override processRules(RuleEngineBlackboard blackboard, Path path) {
		val rawYamls = blackboard.getPartition(YAML_DISCOVERER_ID) as Map<Path, Iterable<Map<String, Object>>>
		val yamlMappers = blackboard.getPartition(YAML_MAPPERS_KEY) as Map<Path, Function<String, Optional<String>>>
		val poms = blackboard.getDiscoveredFiles(XML_DISCOVERER_ID, typeof(Document))
		val propertyFiles = blackboard.getDiscoveredFiles(PROPERTIES_DISCOVERER_ID, typeof(Properties))

		val projectRoot = SpringHelper.findProjectRoot(path, poms)

		var Map<Path, List<GatewayRoute>> routeMap = new HashMap<Path, List<GatewayRoute>>()
		if (blackboard.hasPartition(RULE_ID)) {
			routeMap = blackboard.getPartition(RULE_ID) as Map<Path, List<GatewayRoute>>
		}

		// Execute only once for each Spring application/service
		if(routeMap.containsKey(projectRoot)) return

		val bootstrapYaml = projectRoot === null
				? null
				: yamlMappers.get(
				SpringHelper.findFile(yamlMappers.keySet, projectRoot.resolve("src/main/resources"),
					Set.of("bootstrap.yaml", "bootstrap.yml")))
		val applicationProperties = projectRoot === null
				? null
				: propertyFiles.get(
				SpringHelper.findFile(propertyFiles.keySet, projectRoot.resolve("src/main/resources"),
					Set.of("application.properties")))
		val applicationName = SpringHelper.getFromYamlOrProperties("spring.application.name", bootstrapYaml,
			applicationProperties)
		val rawApplicationYaml = projectRoot === null
				? null
				: rawYamls.get(
				SpringHelper.findFile(yamlMappers.keySet, projectRoot.resolve("src/main/resources"),
					Set.of("application.yaml", "application.yml")))

		// Query spring.cloud.gateway.routes in application.yaml only
		val routes = collectRoutes(rawApplicationYaml)
		for (route : routes) {
			LOG.warn("Route in " + applicationName + ": " + route.path + " -> " + route.getTargetHost)
		}
		routeMap.put(projectRoot, routes)
		blackboard.addPartition(RULE_ID, routeMap)
		if (blackboard.hasPartition(ECMASCRIPT_ROUTES_ID)) {
			val ecmaScriptRouteMap = blackboard.getPartition(ECMASCRIPT_ROUTES_ID) as Map<Path, List<GatewayRoute>>
			if (ecmaScriptRouteMap.containsKey(projectRoot)) {
				ecmaScriptRouteMap.get(projectRoot).addAll(routes)
			} else {
				ecmaScriptRouteMap.put(projectRoot, routes)
			}
			blackboard.addPartition(ECMASCRIPT_ROUTES_ID, ecmaScriptRouteMap)
		} else {
			blackboard.addPartition(ECMASCRIPT_ROUTES_ID, routeMap)
		}

		if (applicationName !== null) {
			var Map<Path, String> hostnameMap = new HashMap<Path, String>()
			if (blackboard.hasPartition(ECMASCRIPT_HOSTNAMES_ID)) {
				hostnameMap = blackboard.getPartition(ECMASCRIPT_HOSTNAMES_ID) as Map<Path, String>
			}
			hostnameMap.put(projectRoot, applicationName)

			blackboard.addPartition(ECMASCRIPT_HOSTNAMES_ID, hostnameMap)
		}
	}

	def List<GatewayRoute> collectRoutes(Iterable<Map<String, Object>> applicationYamlIter) {
		val result = new ArrayList()

		if(applicationYamlIter === null || applicationYamlIter.empty) return result

		val applicationYaml = applicationYamlIter.get(0) as Map<String, Object>
		if(applicationYaml === null) return result

		val springObject = applicationYaml.get("spring")
		if(!(springObject instanceof Map)) return result
		val spring = springObject as Map<String, Object>

		val cloudObject = spring.get("cloud")
		if(!(cloudObject instanceof Map)) return result
		val cloud = cloudObject as Map<String, Object>

		val gatewayObject = cloud.get("gateway")
		if(!(gatewayObject instanceof Map)) return result
		val gateway = gatewayObject as Map<String, Object>

		val routesObject = gateway.get("routes")
		if(!(routesObject instanceof List)) return result
		val routes = routesObject as List<Map<String, Object>>

		for (route : routes) {
			val uriObject = route.get("uri")
			val predicatesObject = route.get("predicates")
			val filtersObject = route.get("filters")

			var path = Optional.empty
			if (predicatesObject !== null && predicatesObject instanceof List) {
				path = getPath(predicatesObject as List<String>)
			}
			var stripPrefixLength = 0
			if (filtersObject !== null && filtersObject instanceof List) {
				stripPrefixLength = getStripPrefixLength(filtersObject as List<String>)
			}
			val hasUri = uriObject !== null && uriObject instanceof String

			if (path.present && hasUri) {
				val uri = uriObject as String
				result.add(new GatewayRoute(path.get, toHostname(uri), stripPrefixLength))
			}
		}

		return result;
	}

	def getPath(List<String> predicates) {
		for (predicate : predicates) {
			val prefix = "Path="
			if (predicate.startsWith(prefix)) {
				return Optional.of(predicate.substring(prefix.length))
			}
		}
		return Optional.empty
	}

	def getStripPrefixLength(List<String> filters) {
		for (filter : filters) {
			val prefix = "StripPrefix="
			if (filter.startsWith(prefix)) {
				val stripPrefixLength = filter.substring(prefix.length)
				try {
					return Integer.parseInt(stripPrefixLength)
				} catch (NumberFormatException e) {
					return 0
				}
			}
		}
		return 0
	}

	def toHostname(String url) {
		var schemaEnd = url.lastIndexOf("://")
		if (schemaEnd == -1) {
			schemaEnd = -3
		}
		val hostnameStart = schemaEnd + 3
		var portIndex = url.indexOf(":", hostnameStart)
		if (portIndex == -1) {
			portIndex = url.length
		}
		var pathIndex = url.indexOf("/", hostnameStart)
		if (pathIndex == -1) {
			pathIndex = url.length
		}
		val hostnameEnd = Math.min(portIndex, pathIndex)
		return url.substring(hostnameStart, hostnameEnd)
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
		return "Spring Cloud Gateway Rules"
	}

	override getRequiredServices() {
		return Set.of(YAML_DISCOVERER_ID, XML_DISCOVERER_ID, PROPERTIES_DISCOVERER_ID)
	}

	override getDependentServices() {
		Set.of(ECMASCRIPT_RULE_ID)
	}
}
