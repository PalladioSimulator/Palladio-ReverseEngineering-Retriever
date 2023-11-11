package org.palladiosimulator.somox.analyzer.rules.impl


import static org.palladiosimulator.somox.analyzer.rules.engine.RuleHelper.*
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard
import java.nio.file.Path
import org.eclipse.jdt.core.dom.CompilationUnit
import java.util.Map;
import org.jdom2.Document
import java.util.stream.Collectors
import org.apache.log4j.Logger
import org.eclipse.jdt.core.dom.MethodDeclaration
import org.eclipse.jdt.core.dom.ITypeBinding
import java.util.HashMap
import java.util.List
import java.util.Properties
import org.palladiosimulator.somox.analyzer.rules.model.RESTName
import org.palladiosimulator.somox.analyzer.rules.model.HTTPMethod
import java.util.Optional
import org.palladiosimulator.somox.analyzer.rules.model.CompUnitOrName
import java.util.function.Function
import java.util.Set
import org.palladiosimulator.somox.analyzer.rules.engine.Rule

class SpringRules implements Rule {
    static final Logger LOG = Logger.getLogger(SpringRules)

    public static final String RULE_ID = "org.palladiosimulator.somox.analyzer.rules.impl.spring"
    public static final String JAVA_DISCOVERER_ID = "org.palladiosimulator.somox.discoverer.java"
    public static final String YAML_DISCOVERER_ID = "org.palladiosimulator.somox.discoverer.yaml"
    public static final String YAML_MAPPERS_KEY = YAML_DISCOVERER_ID + ".mappers"
    public static final String XML_DISCOVERER_ID = "org.palladiosimulator.somox.discoverer.xml"
    public static final String PROPERTIES_DISCOVERER_ID = "org.palladiosimulator.somox.discoverer.properties"

	override processRules(RuleEngineBlackboard blackboard, Path path) {
		val unit = blackboard.getDiscoveredFiles(JAVA_DISCOVERER_ID, typeof(CompilationUnit)).get(path)
		if (unit === null) return;

		val rawYamls = blackboard.getPartition(YAML_DISCOVERER_ID) as Map<Path, Iterable<Map<String, Object>>>
		val yamlMappers = blackboard.getPartition(YAML_MAPPERS_KEY) as Map<Path, Function<String, Optional<String>>>
		val poms = blackboard.getDiscoveredFiles(XML_DISCOVERER_ID, typeof(Document))
		val propertyFiles = blackboard.getDiscoveredFiles(PROPERTIES_DISCOVERER_ID, typeof(Properties))

		val projectRoot = findProjectRoot(path, poms)
		val configRoot = findConfigRoot(poms)
		val bootstrapYaml = yamlMappers.get(findFile(yamlMappers.keySet, projectRoot.resolve("src/main/resources"), Set.of("bootstrap.yaml", "bootstrap.yml")))
		val applicationProperties = propertyFiles.get(findFile(propertyFiles.keySet, projectRoot.resolve("src/main/resources"), Set.of("application.properties")))

		val applicationName = getFromYamlOrProperties("spring.application.name", bootstrapYaml, applicationProperties)
		val projectConfigYaml = yamlMappers.get(findFile(yamlMappers.keySet, configRoot, Set.of(applicationName + ".yaml", applicationName + ".yml")))
		val contextPathOption = Optional.ofNullable(projectConfigYaml).flatMap[x | x.apply("server.servlet.context-path")]
		var contextPath = contextPathOption.orElse("/")
		if (applicationName !== null) contextPath += applicationName + "/"
		
		val rawApplicationYaml = rawYamls.get(findFile(yamlMappers.keySet, projectRoot.resolve("src/main/resources"), Set.of("application.yaml", "application.yml")))
		val contextVariables = collectContextVariables(rawApplicationYaml)
		
		processRuleForCompUnit(blackboard, unit, contextPath, contextVariables)
	}

	def findProjectRoot(Path currentPath, Map<Path, Document> poms) {
		if (currentPath === null || poms === null) {
			return null
		}
		val closestPom = poms.entrySet.stream
			.map([ entry | entry.key ])
			// Only keep poms above the current compilation unit.
			.filter([ path | currentPath.startsWith(path.parent) ])
			// Take the longest path, which is the pom.xml closest to the compilation unit
			.max([a, b | a.size.compareTo(b.size)])

		if (closestPom.present) {
			return closestPom.get.parent
		} else {
			return null
		}
	}

	def findConfigRoot(Map<Path, Document> poms) {
		if (poms === null) {
			return null
		}
		val configRoots = poms.entrySet.stream
			.map[ entry | entry.key -> entry.value.rootElement.getChild("dependencies", entry.value.rootElement.namespace) ]
			.filter[ entry | entry.value !== null ]
			.map[ entry | entry.key -> entry.value.getChildren("dependency", entry.value.namespace) ]
			.filter[ entry | !entry.value.empty ]
			.filter[ entry | entry.value
				.filter[ dependency | dependency.getChildTextTrim("groupId", dependency.namespace) == "org.springframework.cloud" ]
				.exists[ dependency | dependency.getChildTextTrim("artifactId", dependency.namespace) == "spring-cloud-config-server" ]
			]
			.collect(Collectors.toList)
		
		if (configRoots.size > 1) {
			LOG.warn("Multiple Spring config servers, choosing \"" + configRoots.get(0).key.parent + "\" arbitrarily")
		} else if (configRoots.empty) {
			return null
		}
		return configRoots.get(0).key.parent
	}
	
	def findFile(Set<Path> paths, Path directory, Set<String> possibleNames) {
		if (paths === null || directory === null || possibleNames === null) {
			return null
		}
		val candidates =  paths.stream
			.filter[ path | path.parent == directory ]
			.filter[ path | possibleNames.contains(path.fileName.toString) ]
			.collect(Collectors.toList)

		if (candidates.size > 1) {
			// fileName must exist since candidates were found
			val fileName = possibleNames.iterator.next;
			LOG.warn("Multiple " + fileName + " in " + directory + ", choosing " + directory.relativize(candidates.get(0)) + " arbitrarily")
		} else if (candidates.empty) {
			return null
		}
		return candidates.get(0)
	}
	
	def getFromYamlOrProperties(String key, Function<String, Optional<String>> yamlMapper, Properties properties) {
		if (yamlMapper !== null) {
			val result = yamlMapper.apply(key)
			if (result.present) {
				return result.get()
			}
		}
		
		if (properties !== null) {
			return properties.getProperty(key)
		}
		
		return null
	}

	def Map<String, String> collectContextVariables(Iterable<Map<String, Object>> applicationYaml) {
		val result = new HashMap<String, String>();
		if (applicationYaml === null || applicationYaml.empty) {
			return result;
		}
		
		return collectContextVariables(applicationYaml.get(0));
	}
	
	def Map<String, String> collectContextVariables(Map<String, Object> applicationYaml) {
		val result = new HashMap<String, String>();
		if (applicationYaml === null) {
			return result;
		}

		for (entry : applicationYaml.entrySet) {
			if (entry.value instanceof Map) {
				val mapValue = entry.value as Map<String, Object>;
				for (mapEntry : collectContextVariables(mapValue).entrySet) {
					result.put(entry.key + "." + mapEntry.key, mapEntry.value);
				}
			} else if (entry.value instanceof List) {
				val extendedMapValue = entry.value as List<Map<String, Object>>;
				for (extendedEntry : extendedMapValue) {
					val extendedKey = extendedEntry.get("key") as String;
					var extendedValue = extendedEntry.get("value") as String;
					if (extendedKey !== null && extendedValue !== null) {
						if (extendedValue.startsWith("${")) {
							val startIndex = extendedValue.indexOf(":");
							val endIndex = extendedValue.indexOf("}", startIndex);
							extendedValue = extendedValue.substring(startIndex + 1, endIndex);
						}
						result.put(entry.key + "." + extendedKey, extendedValue);
					}
				}
			} else if (entry.value instanceof String) {
				var stringValue = entry.value as String;
				if (stringValue.startsWith("${")) {
					val startIndex = stringValue.indexOf(":");
					val endIndex = stringValue.indexOf("}", startIndex);
					stringValue = stringValue.substring(startIndex + 1, endIndex);
				}
				result.put(entry.key, stringValue);
			}
		}

		return result;
	}

	def processRuleForCompUnit(RuleEngineBlackboard blackboard, CompilationUnit unit, String contextPath, Map<String, String> contextVariables) {
		val pcmDetector = blackboard.getPCMDetector
		if (pcmDetector === null) return;

		// Abort if there is no CompilationUnit at the specified path
		if (unit === null) return;

		val isService = isUnitAnnotatedWithName(unit, "Service")
		val isController = isUnitAnnotatedWithName(unit, "RestController") || isUnitAnnotatedWithName(unit, "Controller")
		val isClient = isUnitAnnotatedWithName(unit, "FeignClient")
		val isRepository = isRepository(unit)
		val isComponent = isService || isController || isClient || isRepository || isUnitAnnotatedWithName(unit, "Component")
		
		val identifier = new CompUnitOrName(unit)

		if (isComponent) {
			pcmDetector.detectComponent(identifier)

			getConstructors(unit)
				.stream
				.filter[c | isMethodAnnotatedWithName(c, "Autowired")]
				.flatMap[c | getParameters(c).stream]
				.filter[p | !isParameterAnnotatedWith(p, "Value")]
				.forEach[p | pcmDetector.detectRequiredInterface(identifier, p)]
		}

		if (isService || isController) {
			for (f : getFields(unit)) {
				val annotated = isFieldAnnotatedWithName(f, "Autowired")
				if (annotated || isRepository(f.type.resolveBinding)) {
					pcmDetector.detectRequiredInterface(identifier, f)
				}
			}
		}

		if (isService || isController) {
			pcmDetector.detectPartOfComposite(identifier, getUnitName(unit));
		}

		if (isController) {
			val requestedUnitMapping = getUnitAnnotationStringValue(unit, "RequestMapping");
			var ifaceName = contextPath;
			if (requestedUnitMapping !== null) {
				ifaceName += requestedUnitMapping;
			}
			for (m : getMethods(unit)) {
				val annotated = hasMapping(m);
				if (annotated) {
					var requestedMapping = getMapping(m);
					if (requestedMapping !== null) {
						requestedMapping = substituteVariables(requestedMapping, contextVariables);
						var methodName = ifaceName + "/" + requestedMapping;
						methodName = replaceArgumentsWithWildcards(methodName);
						val httpMethod = getHTTPMethod(m);
						pcmDetector.detectCompositeProvidedOperation(identifier, m.resolveBinding, new RESTName(methodName, httpMethod));
					}
				}
			}
		}

		if (isClient) {
			val requestedUnitMapping = getUnitAnnotationStringValue(unit, "RequestMapping");
			// Do not include the context path since client requests are expressed as uniquely identifiable paths.
			var ifaceName = "";
			if (requestedUnitMapping !== null) {
				ifaceName += requestedUnitMapping;
			}
			for (m : getMethods(unit)) {
				val annotated = hasMapping(m);
				if (annotated) {
					var requestedMapping = getMapping(m);
					if (requestedMapping !== null) {
						requestedMapping = substituteVariables(requestedMapping, contextVariables);
						var methodName = ifaceName + "/" + requestedMapping;
						methodName = replaceArgumentsWithWildcards(methodName);
						val httpMethod = getHTTPMethod(m);
						pcmDetector.detectCompositeRequiredInterface(identifier, new RESTName(methodName, httpMethod));
					}
				}
			}
		}

		// TODO: This can be solved differently now, for all implemented interfaces instead of just one.
		var inFs = getAllInterfaces(unit)
		val isImplementingOne = inFs.size == 1

		if (isComponent && isImplementingOne) {
			var firstIn = inFs.get(0)
			pcmDetector.detectProvidedInterface(identifier, firstIn.resolveBinding)
			for (m : getMethods(firstIn)) {
				pcmDetector.detectProvidedOperation(identifier, firstIn.resolveBinding, m)
			}
		}
	}
	
	def substituteVariables(String string, Map<String, String> contextVariables) {
		var result = string;

		while (result.contains("${")) {
			val startIndex = result.indexOf("${");
			val endIndex = result.indexOf("}", startIndex);
			val key = result.substring(startIndex + 2, endIndex);
			val value = contextVariables.get(key);
			if (value !== null) {
				result = result.substring(0, startIndex) + value + result.substring(endIndex + 1);
			} else {
				result = result.substring(0, startIndex) + "ERROR_COULD_NOT_RESOLVE" + result.substring(endIndex + 1);
				LOG.error("Could not resolve key " + key);
			}
		}

		return result;
	}
	
	def replaceArgumentsWithWildcards(String methodName) {
		var newName = methodName.replaceAll("\\{.*\\}", "*")
						 .replaceAll("[\\*\\/]*$", "")
						 .replaceAll("[\\*\\/]*\\[", "[")
		newName = "/" + newName
		newName = newName.replaceAll("/+", "/")
		return newName
	}

	def hasMapping(MethodDeclaration m) {
		return isMethodAnnotatedWithName(m, "RequestMapping")
		    ||isMethodAnnotatedWithName(m, "GetMapping")
    		||isMethodAnnotatedWithName(m, "PostMapping")
	    	||isMethodAnnotatedWithName(m, "PutMapping")
		    ||isMethodAnnotatedWithName(m, "DeleteMapping")
    		||isMethodAnnotatedWithName(m, "PatchMapping");
	}

	def getMapping(MethodDeclaration m) {
		val requestMapping = getMappingString(m, "RequestMapping");
		if (requestMapping !== null) {
			return requestMapping;
		}

		val getMapping = getMappingString(m, "GetMapping");
		if (getMapping !== null) {
			return getMapping;
		}

		val postMapping = getMappingString(m, "PostMapping");
		if (postMapping !== null) {
			return postMapping;
		}

		val putMapping = getMappingString(m, "PutMapping");
		if (putMapping !== null) {
			return putMapping;
		}

		val deleteMapping = getMappingString(m, "DeleteMapping");
		if (deleteMapping !== null) {
			return deleteMapping;
		}

		val patchMapping = getMappingString(m, "PatchMapping");
		if (patchMapping !== null) {
			return patchMapping;
		}
		
		return null;
	}

	def getHTTPMethod(MethodDeclaration m) {
		val requestMapping = getMappingString(m, "RequestMapping");
		if (requestMapping !== null) {
			return Optional.empty;
		}

		val getMapping = getMappingString(m, "GetMapping");
		if (getMapping !== null) {
			return Optional.of(HTTPMethod.GET);
		}

		val postMapping = getMappingString(m, "PostMapping");
		if (postMapping !== null) {
			return Optional.of(HTTPMethod.POST);
		}

		val putMapping = getMappingString(m, "PutMapping");
		if (putMapping !== null) {
			return Optional.of(HTTPMethod.PUT);
		}

		val deleteMapping = getMappingString(m, "DeleteMapping");
		if (deleteMapping !== null) {
			return Optional.of(HTTPMethod.DELETE);
		}

		val patchMapping = getMappingString(m, "PatchMapping");
		if (patchMapping !== null) {
			return Optional.of(HTTPMethod.PATCH);
		}
		
		return null;
	}

	def getMappingString(MethodDeclaration m, String annotationName) {
		val value = getMethodAnnotationStringValue(m, annotationName);
		if (value !== null) {
			return value;
		}

		val path = getMethodAnnotationStringValue(m, annotationName, "path");
		return path;
	}
	
	def isRepository(ITypeBinding binding) {
		return isImplementingOrExtending(binding, "Repository")
			|| isImplementingOrExtending(binding, "CrudRepository")
			|| isImplementingOrExtending(binding, "JpaRepository")
			|| isImplementingOrExtending(binding, "PagingAndSortingRepository")
			|| isImplementingOrExtending(binding, "MongoRepository")
	}

	def isRepository(CompilationUnit unit) {
		return isUnitAnnotatedWithName(unit, "Repository") 
			|| isImplementingOrExtending(unit, "Repository")
			|| isImplementingOrExtending(unit, "CrudRepository")
			|| isImplementingOrExtending(unit, "JpaRepository")
			|| isImplementingOrExtending(unit, "PagingAndSortingRepository")
			|| isImplementingOrExtending(unit, "MongoRepository")
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
		return "Spring Rules"
	}
	
	override getRequiredServices() {
		return Set.of(JAVA_DISCOVERER_ID, YAML_DISCOVERER_ID, XML_DISCOVERER_ID, PROPERTIES_DISCOVERER_ID)
	}
	
}
