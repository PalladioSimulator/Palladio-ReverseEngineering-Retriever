package org.palladiosimulator.retriever.extraction.rules.util

import java.nio.file.Path
import java.util.Map
import org.jdom2.Document
import java.util.Set

class ProjectHelper {
	static def findMavenProjectRoot(Path currentPath, Map<Path, Document> xmls) {
		if (currentPath === null || xmls === null) {
			return null
		}
		val closestPom = xmls.keySet.stream // Only keep poms above the current compilation unit.
		.filter[path|path.fileName.toString == "pom.xml"]
		.filter[path|currentPath.startsWith(path.parent)] // Take the longest path, which is the pom.xml closest to the compilation unit
		.max([a, b|a.size.compareTo(b.size)])

		if (closestPom.present) {
			return closestPom.get.parent
		} else {
			return null
		}
	}

	static def findProjectRoot(Path currentPath, Set<Path> systemRoots) {
		if (currentPath === null || systemRoots === null) {
			return null
		}
		val closestSystemRoot = systemRoots.stream // Only keep build files above the current compilation unit.
		.filter([path|currentPath.startsWith(path.parent)]) // Take the longest path, which is the build file closest to the compilation unit
		.max([a, b|a.size.compareTo(b.size)])

		if (closestSystemRoot.present) {
			return closestSystemRoot.get.parent
		} else {
			return null
		}
	}
}
