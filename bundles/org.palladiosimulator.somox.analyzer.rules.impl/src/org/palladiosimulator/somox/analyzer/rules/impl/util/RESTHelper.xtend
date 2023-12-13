package org.palladiosimulator.somox.analyzer.rules.impl.util

class RESTHelper {
	def static replaceArgumentsWithWildcards(String methodName) {
		var newName = methodName.replaceAll("\\{.*\\}", "*").replaceAll("[\\*\\/]*$", "").replaceAll("[\\*\\/]*\\[",
			"[")
		newName = "/" + newName
		newName = newName.replaceAll("/+", "/")
		return newName
	}
}