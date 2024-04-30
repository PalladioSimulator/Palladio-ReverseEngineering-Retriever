package org.palladiosimulator.retriever.extraction.rules.util

import java.nio.file.Path
import java.util.Map
import org.jdom2.Document
import java.nio.file.Files

class ProjectHelper {
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
