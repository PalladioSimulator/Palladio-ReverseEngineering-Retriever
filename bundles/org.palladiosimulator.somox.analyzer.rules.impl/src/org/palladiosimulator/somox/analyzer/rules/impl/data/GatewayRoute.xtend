package org.palladiosimulator.somox.analyzer.rules.impl.data

import org.palladiosimulator.somox.analyzer.rules.model.RESTName
import java.util.Optional

class GatewayRoute {
	String pathPattern
	String targetHost
	boolean stripPrefix

	new(String path, String serviceId, boolean stripPrefix) {
		this.pathPattern = path
		this.targetHost = serviceId
		this.stripPrefix = stripPrefix
	}

	def getPath() { pathPattern }

	def getTargetHost() { targetHost }

	def matches(String url) {
		if (pathPattern.endsWith("/**")) {
			val prefix = pathPattern.substring(0, pathPattern.length - 3)
			return url.startsWith(prefix)
		}
		return pathPattern.equals(url)
	}

	// Only well-defined if this.matches(url)
	def RESTName applyTo(String url) {
		if (pathPattern.endsWith("/**")) {
			if (!stripPrefix) {
				return new RESTName(targetHost, url, Optional.empty)
			}
			val newUrl = url.substring(pathPattern.length - 3)
			return new RESTName(targetHost, newUrl, Optional.empty)
		}
		return new RESTName(targetHost, url, Optional.empty)
	}
}
