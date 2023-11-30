package org.palladiosimulator.somox.analyzer.rules.impl.util

import java.nio.file.Path
import java.util.Map
import java.util.Optional
import java.util.Properties
import java.util.Set
import java.util.function.Function
import java.util.stream.Collectors
import org.jdom2.Document
import org.apache.log4j.Logger

final class SpringHelper {
	static final Logger LOG = Logger.getLogger(SpringHelper)

	new() {
		throw new IllegalAccessException()
	}

	static def findProjectRoot(Path currentPath, Map<Path, Document> poms) {
		if (currentPath === null || poms === null) {
			return null
		}
		val closestPom = poms.entrySet.stream.map([entry|entry.key]) // Only keep poms above the current compilation unit.
		.filter([path|currentPath.startsWith(path.parent)]) // Take the longest path, which is the pom.xml closest to the compilation unit
		.max([a, b|a.size.compareTo(b.size)])

		if (closestPom.present) {
			return closestPom.get.parent
		} else {
			return null
		}
	}

	static def findConfigRoot(Map<Path, Document> poms) {
		if (poms === null) {
			return null
		}
		val configRoots = poms.entrySet.stream.map [ entry |
			entry.key -> entry.value.rootElement.getChild("dependencies", entry.value.rootElement.namespace)
		].filter[entry|entry.value !== null].map [ entry |
			entry.key -> entry.value.getChildren("dependency", entry.value.namespace)
		].filter[entry|!entry.value.empty].filter [ entry |
			entry.value.filter [ dependency |
				dependency.getChildTextTrim("groupId", dependency.namespace) == "org.springframework.cloud"
			].exists [ dependency |
				dependency.getChildTextTrim("artifactId", dependency.namespace) == "spring-cloud-config-server"
			]
		].collect(Collectors.toList)

		if (configRoots.size > 1) {
			LOG.warn("Multiple Spring config servers, choosing \"" + configRoots.get(0).key.parent + "\" arbitrarily")
		} else if (configRoots.empty) {
			return null
		}
		return configRoots.get(0).key.parent
	}

	static def findFile(Set<Path> paths, Path directory, Set<String> possibleNames) {
		if (paths === null || directory === null || possibleNames === null) {
			return null
		}
		val candidates = paths.stream.filter[path|path.parent == directory].filter [ path |
			possibleNames.contains(path.fileName.toString)
		].collect(Collectors.toList)

		if (candidates.size > 1) {
			// fileName must exist since candidates were found
			val fileName = possibleNames.iterator.next;
			LOG.warn(
				"Multiple " + fileName + " in " + directory + ", choosing " + directory.relativize(candidates.get(0)) +
					" arbitrarily")
		} else if (candidates.empty) {
			return null
		}
		return candidates.get(0)
	}

	static def getFromYamlOrProperties(String key, Function<String, Optional<String>> yamlMapper,
		Properties properties) {
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
}
