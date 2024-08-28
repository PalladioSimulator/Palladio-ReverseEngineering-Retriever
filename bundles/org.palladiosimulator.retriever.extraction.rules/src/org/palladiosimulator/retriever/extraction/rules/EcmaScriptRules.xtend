package org.palladiosimulator.retriever.extraction.rules

import org.openjdk.nashorn.api.tree.CompilationUnitTree
import java.nio.file.Path
import java.util.Map
import java.util.HashSet
import java.util.Set
import org.openjdk.nashorn.api.tree.Tree
import org.openjdk.nashorn.api.tree.SimpleTreeVisitorES6
import org.openjdk.nashorn.api.tree.FunctionCallTree
import org.openjdk.nashorn.api.tree.MemberSelectTree
import java.util.Arrays
import java.util.HashMap
import org.openjdk.nashorn.api.tree.IdentifierTree
import org.openjdk.nashorn.api.tree.LiteralTree
import org.openjdk.nashorn.api.tree.FunctionDeclarationTree
import org.openjdk.nashorn.api.tree.ExpressionTree
import org.openjdk.nashorn.api.tree.BinaryTree
import org.openjdk.nashorn.api.tree.ObjectLiteralTree
import java.util.List
import org.openjdk.nashorn.api.tree.VariableTree
import org.palladiosimulator.retriever.extraction.commonalities.RESTName
import org.palladiosimulator.retriever.extraction.commonalities.CompUnitOrName
import org.palladiosimulator.retriever.extraction.rules.data.GatewayRoute
import org.palladiosimulator.retriever.extraction.commonalities.RESTOperationName
import org.palladiosimulator.retriever.services.blackboard.RetrieverBlackboard
import org.palladiosimulator.retriever.services.Rule
import org.palladiosimulator.retriever.extraction.engine.PCMDetector

class EcmaScriptRules implements Rule {

	public static final String RULE_ID = "org.palladiosimulator.retriever.extraction.rules.ecmascript"

	public static final String ECMASCRIPT_DISCOVERER_ID = "org.palladiosimulator.retriever.extraction.discoverers.ecmascript"
	public static final String HOSTNAMES_ID = "org.palladiosimulator.retriever.extraction.rules.ecmascript.hostnames"
	public static final String GATEWAY_ROUTES_ID = "org.palladiosimulator.retriever.extraction.rules.ecmascript.routes"
	public static final String DONE_ID = "org.palladiosimulator.retriever.extraction.rules.ecmascript.done"

	static final CompUnitOrName GATEWAY_NAME = new CompUnitOrName("Gateway")
	static final String START_NONWORD_CHARS = "^[\\W]+"
	static final String SEPARATOR = "."
	static final String URL_KEYWORD = "url"
	static final String HTTP_KEYWORD = "http"
	static final String[] HTTP_REQUESTS = #["connect", "delete", "get", "head", "options", "patch", "post", "put",
		"trace"]
	static final String VARIABLE_PREFIX = ":"
	static final String BLANK = ""

	override processRules(RetrieverBlackboard blackboard, Path path) {
		if (blackboard.hasPartition(DONE_ID)) return

		val compilationUnits = blackboard.getDiscoveredFiles(ECMASCRIPT_DISCOVERER_ID, typeof(CompilationUnitTree))
		val compilationUnit = compilationUnits.get(path)
		if (compilationUnit === null && !compilationUnits.empty) {
			return
		}

		var gatewayRouteMap = blackboard.getPartition(GATEWAY_ROUTES_ID) as Map<Path, List<GatewayRoute>>
		if (gatewayRouteMap === null) {
			gatewayRouteMap = Map.of
		}
		var gatewayRoutes = List.of
		var Path mostSpecificGatewayPath = null
		for (gatewayPath : gatewayRouteMap.keySet) {
			if (gatewayPath !== null && path.startsWith(gatewayPath) &&
				(mostSpecificGatewayPath === null || gatewayPath.startsWith(mostSpecificGatewayPath))) {
				gatewayRoutes = gatewayRouteMap.get(gatewayPath)
				mostSpecificGatewayPath = gatewayPath
			}
		}

		var hostnameMap = Map.of 
		if (blackboard.hasPartition(HOSTNAMES_ID)) {
			hostnameMap = blackboard.getPartition(HOSTNAMES_ID) as Map<Path, String>
		}
		var hostname = "API-HOST"
		var Path mostSpecificHostnamePath = null
		for (hostnamePath : hostnameMap.keySet) {
			if (hostnamePath !== null && path.startsWith(hostnamePath) &&
				(mostSpecificHostnamePath === null || hostnamePath.startsWith(mostSpecificHostnamePath))) {
				hostname = hostnameMap.get(hostnamePath)
				mostSpecificHostnamePath = hostnamePath
			}
		}
		
		var httpRequests = Map.of
		val pcmDetector = blackboard.getPCMDetector as PCMDetector
		if (!compilationUnits.empty) {
			httpRequests = findAllHttpRequests(blackboard, compilationUnit)
			for (key : httpRequests.keySet) {
				for (url : httpRequests.get(key)) {
					val mappedURL = mapURL(hostname, "/" + url, gatewayRoutes)
					if (!mappedURL.isPartOf("/" + hostname)) {
						pcmDetector.detectCompositeRequiredInterface(GATEWAY_NAME, mappedURL)
					}
					pcmDetector.detectProvidedOperation(GATEWAY_NAME, null, new RESTOperationName(hostname, "/" + url))
				}
			}
		}
		
		// Require all routes if no requests could be parsed
		if (httpRequests.empty) {
			for (route : gatewayRoutes) {
				val mappedURL = new RESTName(route.targetHost, "/")
				if (!mappedURL.isPartOf("/" + hostname)) {
					pcmDetector.detectCompositeRequiredInterface(GATEWAY_NAME, mappedURL)
				}
				pcmDetector.detectProvidedOperation(GATEWAY_NAME, null, new RESTOperationName(hostname, "/"))
			}
			blackboard.addPartition(DONE_ID, true)
		}
	}

	def findAllHttpRequests(RetrieverBlackboard blackboard, CompilationUnitTree unit) {
		val source = unit.getSourceName().substring(0, unit.getSourceName().lastIndexOf(SEPARATOR) + 1)
		val assignments = findVariableAssignments(unit)
		val requests = join(findFunctionCallsWithUrls(unit), findFunctionDeclarationsWithUrls(unit),
			findDirectHttpRequest(unit))
		val Map<String, Set<String>> normalizedRequests = new HashMap()
		for (key : requests.keySet()) {
			var resolvedUrls = new HashSet()
			for (url : requests.get(key)) {
				if (url.startsWith(VARIABLE_PREFIX)) {
					if (assignments.containsKey(url)) {
						resolvedUrls.add(assignments.get(url))
					}
				} else {
					resolvedUrls.add(url)
				}
			}
			var urlsWithWildcards = new HashSet()
			for (url : resolvedUrls) {
				val urlWithoutInteriorParameters = url.replaceAll(VARIABLE_PREFIX + ".+?/", "*/")
				val urlWithoutParameters = urlWithoutInteriorParameters.replaceAll(VARIABLE_PREFIX + ".*", "*")
				urlsWithWildcards.add(urlWithoutParameters)
			}

			normalizedRequests.put(source + key.replaceAll(START_NONWORD_CHARS, BLANK), urlsWithWildcards)
		}

		return normalizedRequests
	}

	def Map<String, Set<String>> findDirectHttpRequest(Tree element) {
		val calls = new HashMap()
		element.accept(new SimpleTreeVisitorES6<Void, Void>() {
			override visitFunctionCall(FunctionCallTree node, Void v) {
				val memberObject = node.getFunctionSelect()
				if (memberObject instanceof MemberSelectTree && (Arrays.stream(HTTP_REQUESTS).filter [ r |
					r.equalsIgnoreCase((memberObject as MemberSelectTree).getIdentifier())
				].findAny().isPresent()) &&
					((memberObject as MemberSelectTree).getExpression() instanceof IdentifierTree) &&
					((memberObject as MemberSelectTree).getExpression() as IdentifierTree).getName().toLowerCase().
						contains(HTTP_KEYWORD)) {
					val member = memberObject as MemberSelectTree
					val identifier = member.getExpression() as IdentifierTree
					val caller = identifier.getName() + SEPARATOR + member.getIdentifier()
					val urls = findLiteralsInArguments(node.getArguments())
					if (!urls.isEmpty()) {
						if (calls.containsKey(caller)) {
							calls.get(caller).addAll(urls)
						} else {
							calls.put(caller, urls)
						}
					}
				}
				return super.visitFunctionCall(node, null)
			}
		}, null);
		return calls;
	}

	def findFunctionCallsWithUrls(Tree element) {
		val calls = new HashMap();
		element.accept(new SimpleTreeVisitorES6<Void, Void>() {
			override visitFunctionCall(FunctionCallTree functionCallTree, Void v) {
				val urls = new HashSet()
				var literalValue = BLANK
				for (argument : functionCallTree.arguments) {
					urls.addAll(findLiteralsForIdentifier(argument, URL_KEYWORD))
				}
				if (urls.empty) {
					return null
				}
				if ((functionCallTree.functionSelect instanceof MemberSelectTree) &&
					((functionCallTree.functionSelect as MemberSelectTree).expression instanceof FunctionCallTree)) {
					if (((functionCallTree.functionSelect as MemberSelectTree).expression as FunctionCallTree).
						arguments.empty) {
						return super.visitFunctionCall(functionCallTree, null);
					}
					if (((functionCallTree.functionSelect as MemberSelectTree).expression as FunctionCallTree).
						arguments.get(0) instanceof LiteralTree) {
						val literal = ((functionCallTree.functionSelect as MemberSelectTree).
							expression as FunctionCallTree).arguments.get(0) as LiteralTree
						literalValue = String.valueOf(literal.value)
					}
				}
				if (!literalValue.blank) {
					calls.put(literalValue, urls)
				}
				return super.visitFunctionCall(functionCallTree, null)
			}
		}, null);
		return calls;
	}

	def Map<String, Set<String>> findFunctionDeclarationsWithUrls(Tree element) {
		val declarations = new HashMap();
		element.accept(new SimpleTreeVisitorES6<Void, Void>() {
			override visitFunctionDeclaration(FunctionDeclarationTree functionDeclaration, Void v) {
				val urls = findLiteralsForIdentifier(functionDeclaration, URL_KEYWORD)
				if (!urls.empty) {
					if (declarations.containsKey(functionDeclaration)) {
						declarations.get(functionDeclaration.name.name).addAll(urls)
					} else {
						declarations.put(functionDeclaration.name.name, urls)
					}
				}
				return super.visitFunctionDeclaration(functionDeclaration, null)
			}
		}, null)
		return declarations
	}

	def String findLiteralInExpression(ExpressionTree expression) {
		if (expression !== null) {
			switch expression {
				LiteralTree:
					return String.valueOf(expression.value)
				IdentifierTree:
					return VARIABLE_PREFIX + String.valueOf(expression.name)
				MemberSelectTree:
					return VARIABLE_PREFIX + String.valueOf(expression.identifier)
				BinaryTree:
					return findLiteralInExpression(expression.leftOperand) +
						findLiteralInExpression(expression.rightOperand)
			}
		}
		return BLANK
	}

	def findLiteralsForIdentifier(Tree element, String identifier) {
		val literals = new HashSet()
		element.accept(new SimpleTreeVisitorES6<Void, Void>() {
			override visitObjectLiteral(ObjectLiteralTree objectLiteral, Void v) {
				for (property : objectLiteral.properties) {
					if (((property.key instanceof IdentifierTree) &&
						identifier.equalsIgnoreCase((property.key as IdentifierTree).name))) {
						literals.add(findLiteralInExpression(property.value))
						return super.visitObjectLiteral(objectLiteral, null)
					}
				}
				return super.visitObjectLiteral(objectLiteral, null)
			}
		}, null)
		return literals
	}

	def findLiteralsInArguments(List<? extends ExpressionTree> arguments) {
		val urls = new HashSet()
		for (argument : arguments) {
			val url = findLiteralInExpression(argument)
			if (!url.blank) {
				urls.add(url)
			}
		}
		return urls
	}

	def findVariableAssignments(Tree element) {
		val assignments = new HashMap()
		element.accept(new SimpleTreeVisitorES6<Void, Void>() {
			override visitVariable(VariableTree node, Void v) {
				val binding = node.getBinding()
				val id = switch binding {
					MemberSelectTree: binding.identifier
					IdentifierTree: binding.name
					default: BLANK
				}
				val url = findLiteralInExpression(node.getInitializer())
				if (!id.blank && !url.blank) {
					assignments.put(VARIABLE_PREFIX + id, url)
				}
				return super.visitVariable(node, null);
			}
		}, null)
		return assignments
	}

	def static join(Map<String, ? extends Set<String>>... maps) {
		val join = new HashMap()
		for (map : maps) {
			for (key : map.keySet) {
				if (join.containsKey(key)) {
					join.get(key).addAll(map.get(key))
				} else {
					join.put(key, map.get(key))
				}
			}
		}
		return join
	}

	def mapURL(String host, String url, List<GatewayRoute> routes) {
		for (route : routes) {
			if (route.matches(url)) {
				return route.applyTo(url)
			}
		}
		return new RESTName(host, url)
	}

	override isBuildRule() {
		return false
	}

	override getConfigurationKeys() {
		return Set.of
	}

	override getID() {
		return RULE_ID
	}

	override getName() {
		return "ECMAScript Rules"
	}

	override getRequiredServices() {
		return Set.of(ECMASCRIPT_DISCOVERER_ID)
	}

	override getDependentServices() {
		Set.of
	}
}
