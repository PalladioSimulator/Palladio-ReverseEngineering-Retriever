package org.palladiosimulator.retriever.extraction.rules.util

import java.nio.file.Path
import java.util.Map
import org.jdom2.Document
import java.nio.file.Files

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

	static def findProjectRoot(Path pathInProject, String... projectFileNames) {
		if (pathInProject === null) {
			return null
		}
		var currentPath = pathInProject
		while (currentPath.size > 0) {
			currentPath = currentPath.parent
			for (projectFileName : projectFileNames) {
				if (Files.exists(currentPath.resolve(projectFileName))) {
					return currentPath
				}
			}
		}
		return null
	}
}
