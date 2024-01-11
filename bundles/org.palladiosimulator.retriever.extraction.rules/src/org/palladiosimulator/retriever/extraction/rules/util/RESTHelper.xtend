package org.palladiosimulator.retriever.extraction.rules.util

class RESTHelper {
	def static replaceArgumentsWithWildcards(String methodName) {
		var newName = methodName.replaceAll("\\{.*\\}", "*").replaceAll("[\\*\\/]*$", "").replaceAll("[\\*\\/]*\\[",
			"[")
		newName = "/" + newName
		newName = newName.replaceAll("/+", "/")
		return newName
	}
}