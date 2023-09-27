package org.palladiosimulator.somox.analyzer.rules.impl

import org.palladiosimulator.somox.analyzer.rules.engine.IRule
import org.openjdk.nashorn.api.tree.CompilationUnitTree
import java.nio.file.Path
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard
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

class EcmaScriptRules extends IRule {

	public static final String ECMASCRIPT_DISCOVERER_ID = "org.palladiosimulator.somox.discoverer.ecmascript"

	static final String START_NONWORD_CHARS = "^[\\W]+"
	static final String SEPARATOR = "."
	static final String URL_KEYWORD = "url"
	static final String HTTP_KEYWORD = "http"
	static final String[] HTTP_REQUESTS = #["connect", "delete", "get", "head", "options", "patch", "post", "put",
		"trace"]
	static final String VARIABLE_PREFIX = ":"
	static final String BLANK = ""

	new(RuleEngineBlackboard blackboard) {
		super(blackboard)
	}

	override processRules(Path path) {
		val compilationUnitObjects = blackboard.getPartition(ECMASCRIPT_DISCOVERER_ID)
		val compilationUnits = compilationUnitObjects as Map<String, CompilationUnitTree>
		if(compilationUnits === null) return false
		val compilationUnit = compilationUnits.get(path)
		if(compilationUnit === null) return false
		val httpRequests = findAllHttpRequests(compilationUnit)
		for (key : httpRequests.keySet) {
			System.out.println("\t" + key + " = " + httpRequests.get(key));
		}
		return true
	}

	def findAllHttpRequests(CompilationUnitTree unit) {
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
			normalizedRequests.put(source + key.replaceAll(START_NONWORD_CHARS, BLANK), resolvedUrls)
		}

		return normalizedRequests
	}

	def findDirectHttpRequest(Tree element) {
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
						calls.put(caller, urls)
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

	def findFunctionDeclarationsWithUrls(Tree element) {
		val declarations = new HashMap();
		element.accept(new SimpleTreeVisitorES6<Void, Void>() {
			override visitFunctionDeclaration(FunctionDeclarationTree functionDeclaration, Void v) {
				val urls = findLiteralsForIdentifier(functionDeclaration, URL_KEYWORD)
				if (!urls.empty) {
					declarations.put(functionDeclaration.name.name, urls)
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

}