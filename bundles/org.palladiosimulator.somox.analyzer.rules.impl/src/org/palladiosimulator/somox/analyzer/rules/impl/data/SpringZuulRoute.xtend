package org.palladiosimulator.somox.analyzer.rules.impl.data

class SpringZuulRoute {
	String path
	String serviceId

	new(String path, String serviceId) {
		this.path = path
		this.serviceId = serviceId
	}

	def getPath() { path }
	def getServiceId() { serviceId }
}
