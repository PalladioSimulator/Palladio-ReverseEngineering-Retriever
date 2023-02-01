package org.palladiosimulator.somox.analyzer.rules.impl.eclipse

import static org.palladiosimulator.somox.analyzer.rules.engine.EclipseRuleHelper.*
import org.palladiosimulator.somox.analyzer.rules.engine.IRule
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard
import java.nio.file.Path
import org.eclipse.jdt.core.dom.CompilationUnit
import java.util.Map;
import org.jdom2.Document
import java.util.stream.Collectors
import org.apache.log4j.Logger
import org.eclipse.jdt.core.dom.MethodDeclaration
import org.eclipse.jdt.core.dom.ITypeBinding

class PiggymetricsRules extends IRule {
    static final Logger LOG = Logger.getLogger(PiggymetricsRules)

    public static final String YAML_DISCOVERER_ID = "org.palladiosimulator.somox.discoverer.yaml"
    public static final String XML_DISCOVERER_ID = "org.palladiosimulator.somox.discoverer.xml"

	new(RuleEngineBlackboard blackboard) {
		super(blackboard)
	}

	override boolean processRules(Path path) {
		val yamlObjects = blackboard.getPartition(YAML_DISCOVERER_ID)
		val yamls = yamlObjects as Map<String, Iterable<Map<String, Object>>>

		val pomObjects = blackboard.getPartition(XML_DISCOVERER_ID)
		val poms = pomObjects as Map<String, Document>

		val projectRoot = getProjectRoot(path, poms)
		val configRoot = getConfigRoot(poms)
		val bootstrapYaml = getBootstrapYaml(projectRoot, yamls)
		val applicationName = getApplicationName(bootstrapYaml)
		val projectConfigYaml = getProjectConfigYaml(configRoot, yamls, applicationName)
		val contextPath = getContextPath(projectConfigYaml)

		val units = blackboard.getCompilationUnitAt(path)

		var containedSuccessful = false
		for (unit : units) {
			if (unit.isEclipseCompilationUnit) {
				val eclipseUnit = unit.getEclipseCompilationUnit
				containedSuccessful = processRuleForCompUnit(eclipseUnit, contextPath) || containedSuccessful
			}
		}

		return containedSuccessful
	}

	def getProjectRoot(Path currentPath, Map<String, Document> poms) {
		if (currentPath === null || poms === null) {
			return null
		}
		val closestPom = poms.entrySet.stream
			.map([ entry | Path.of(entry.key) ])
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

	def getConfigRoot(Map<String, Document> poms) {
		if (poms === null) {
			return null
		}
		val configRoots = poms.entrySet.stream
			.map[ entry | Path.of(entry.key) -> entry.value ]
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

	def getBootstrapYaml(Path projectRoot, Map<String, Iterable<Map<String, Object>>> yamls) {
		if (projectRoot === null || yamls === null) {
			return null
		}
		val bootstrapYamls =  yamls.entrySet.stream
			.map[ entry | Path.of(entry.key) -> entry.value ]
			.filter[ entry | entry.key.parent == projectRoot.resolve("src/main/resources") ]
			.filter[ entry | val fileName = entry.key.fileName.toString; fileName == "bootstrap.yaml" || fileName == "bootstrap.yml" ]
			.collect(Collectors.toList)

		if (bootstrapYamls.size > 1) {
			LOG.warn("Multiple bootstrap.y[a]mls in " + projectRoot + ", choosing " + projectRoot.relativize(bootstrapYamls.get(0).key) + " arbitrarily")
		} else if (bootstrapYamls.empty) {
			return null
		}
		return bootstrapYamls.get(0).value
	}

	def getApplicationName(Iterable<Map<String, Object>> bootstrapYaml) {
		if (bootstrapYaml === null) {
			return null
		}
		for (yaml : bootstrapYaml) {
			val applicationName = getApplicationName(yaml)
			if (applicationName != "/") {
				return applicationName
			}
		}
		return "/"
	}

	def getApplicationName(Map<String, Object> bootstrapYaml) {
		val spring = bootstrapYaml.get("spring") as Map<String, Object>
		val application = spring.get("application") as Map<String, Object>
		val name = application.get("name") as String
		return name
	}

	def getProjectConfigYaml(Path configRoot, Map<String, Iterable<Map<String, Object>>> yamls, String projectName) {
		if (configRoot === null || yamls === null || projectName === null) {
			return null
		}
		val projectYamls = yamls.entrySet.stream
			.map[ entry | Path.of(entry.key) -> entry.value ]
			.filter[ entry | entry.key.startsWith(configRoot) ]
			.filter[ entry | val fileName = entry.key.fileName.toString; fileName == projectName + ".yaml" || fileName == projectName + ".yml" ]
			.collect(Collectors.toList)

		if (projectYamls.size > 1) {
			LOG.warn("Multiple " + projectName + ".y[a]mls in config server, choosing " + configRoot.relativize(projectYamls.get(0).key) + " arbitrarily")
		}
		return projectYamls.get(0).value
	}

	def getContextPath(Iterable<Map<String, Object>> projectConfigYaml) {
		if (projectConfigYaml === null) {
			return "/"
		}
		for (yaml : projectConfigYaml) {
			val contextPath = getContextPath(yaml)
			if (contextPath != "/") {
				return contextPath
			}
		}
		return "/"
	}

	def getContextPath(Map<String, Object> projectConfigYaml) {
		val server = projectConfigYaml.get("server") as Map<String, Object>
		if (server === null) {
			return "/"
		}
		val servlet = server.get("servlet") as Map<String, Object>
		if (servlet === null) {
			return "/"
		}
		val contextPath = servlet.get("context-path") as String
		if (contextPath === null) {
			return "/"
		}
		return contextPath
	}

	def boolean processRuleForCompUnit(CompilationUnit unit, String contextPath) {
		val pcmDetector = blackboard.eclipsePCMDetector
		if (pcmDetector === null) {
			return false
		}

		// Abort if there is no CompilationUnit at the specified path
		if (unit === null) {
			return false
		}

		val isService = isUnitAnnotatedWithName(unit, "Service")
		val isController = isUnitAnnotatedWithName(unit, "RestController") || isUnitAnnotatedWithName(unit, "Controller")
		val isClient = isUnitAnnotatedWithName(unit, "FeignClient")
		val isRepository = isRepository(unit)
		val isComponent = isService || isController || isClient || isRepository

		if (isComponent) {
			pcmDetector.detectComponent(unit)
		}

		if (isService || isController) {
			for (f : getFields(unit)) {
				val annotated = isFieldAnnotatedWithName(f, "Autowired")
				if (annotated || isRepository(f.type.resolveBinding)) {
					pcmDetector.detectRequiredInterface(unit, f)
				}
			}
		}

		if (isService || isController) {
			pcmDetector.detectPartOfComposite(unit, getUnitName(unit));
		}

		if (isController) {
			val requestedUnitMapping = getUnitAnnotationStringValue(unit, "RequestMapping");
			var ifaceName = contextPath;
			if (requestedUnitMapping !== null) {
				ifaceName += requestedUnitMapping;
			}
			// Remove leading "//". This can occur if requestedUnitMapping
			// has a leading "/" and the contextPath is "/".
			ifaceName = ifaceName.replace("//", '/');
			pcmDetector.detectOperationInterface(unit, ifaceName);
			for (m : getMethods(unit)) {
				val annotated = hasMapping(m);
				if (annotated) {
					pcmDetector.detectCompositeProvidedOperation(unit, ifaceName, m.resolveBinding);
				}
			}
		}

		if (isClient) {
			for (m : getMethods(unit)) {
				val annotated = hasMapping(m);
				if (annotated) {
					var requestedMapping = getMapping(m);
					var ifaceName = requestedMapping;
					val argumentIndex = requestedMapping.indexOf('{');
					if (argumentIndex >= 0) {
						val lastSegmentStart = requestedMapping.lastIndexOf('/', argumentIndex)
						ifaceName = requestedMapping.substring(0, lastSegmentStart);
					}
					pcmDetector.detectCompositeRequiredInterface(unit, ifaceName);
				}
			}
		}

		var inFs = getAllInterfaces(unit)
		val isImplementingOne = inFs.size == 1

		if (isComponent && isImplementingOne) {
			var firstIn = inFs.get(0)
			pcmDetector.detectOperationInterface(firstIn)
			for (m : getMethods(firstIn)) {
				pcmDetector.detectProvidedOperation(unit, firstIn.resolveBinding, m)
			}
		}

		return true;
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
		return patchMapping;
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
}
