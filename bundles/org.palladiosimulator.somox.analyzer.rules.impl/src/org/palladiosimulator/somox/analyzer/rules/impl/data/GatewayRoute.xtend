package org.palladiosimulator.somox.analyzer.rules.impl.data

import org.palladiosimulator.somox.analyzer.rules.model.RESTName
import java.util.Optional

class GatewayRoute {
	static final String TRAILING_WILDCARD = "/**"

	String pathPattern
	String targetHost
	int stripPrefixLength

	new(String path, String serviceId, boolean stripPrefix) {
		this.pathPattern = path
		this.targetHost = serviceId
		this.stripPrefixLength = stripPrefix ? calculateStripPrefixLength(pathPattern) : 0
	}

	new(String path, String serviceId, int stripPrefixLength) {
		this.pathPattern = path
		this.targetHost = serviceId
		this.stripPrefixLength = stripPrefixLength
	}

	def getPath() { pathPattern }

	def getTargetHost() { targetHost }

	def matches(String url) {
		if (pathPattern.endsWith(TRAILING_WILDCARD)) {
			val prefix = pathPattern.substring(0, pathPattern.length - TRAILING_WILDCARD.length)
			return url.startsWith(prefix)
		}
		return pathPattern.equals(url)
	}

	// Only well-defined if this.matches(url)
	def RESTName applyTo(String url) {
		val urlSegments = toSegments(url)
		var newUrl = ""
		if (stripPrefixLength < urlSegments.length) {
			val newUrlSegments = urlSegments.subList(stripPrefixLength, urlSegments.length)
			newUrl = toPath(newUrlSegments)
		}
		return new RESTName(targetHost, newUrl, Optional.empty)
	}

	private static def calculateStripPrefixLength(String pathPattern) {
		if (pathPattern.endsWith(TRAILING_WILDCARD)) {
			return toSegments(pathPattern).length
		} else {
			return 0
		}
	}

	private static def String[] toSegments(String path) {
		val segments = path.split("/")
		if (segments.length > 0 && segments.get(0).empty) {
			// Skip leading "/"
			return segments.subList(1, segments.length)
		}
		return segments
	}

	private static def toPath(String[] segments) {
		return "/" + segments.join('/')
	}
}
