package org.palladiosimulator.somox.analyzer.rules.ecma;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openjdk.nashorn.api.scripting.NashornException;
import org.openjdk.nashorn.api.tree.BinaryTree;
import org.openjdk.nashorn.api.tree.CompilationUnitTree;
import org.openjdk.nashorn.api.tree.ExpressionTree;
import org.openjdk.nashorn.api.tree.FunctionCallTree;
import org.openjdk.nashorn.api.tree.FunctionDeclarationTree;
import org.openjdk.nashorn.api.tree.IdentifierTree;
import org.openjdk.nashorn.api.tree.LiteralTree;
import org.openjdk.nashorn.api.tree.MemberSelectTree;
import org.openjdk.nashorn.api.tree.ObjectLiteralTree;
import org.openjdk.nashorn.api.tree.Parser;
import org.openjdk.nashorn.api.tree.PropertyTree;
import org.openjdk.nashorn.api.tree.SimpleTreeVisitorES6;
import org.openjdk.nashorn.api.tree.Tree;
import org.openjdk.nashorn.api.tree.VariableTree;

public class EcmaScript {

    private static final String START_NONWORD_CHARS = "^[\\W]+";
    private static final String SEPARATOR = ".";
    private static final String URL_KEYWORD = "url";
    private static final String HTTP_KEYWORD = "http";
    private static final String[] HTTP_REQUESTS = { "connect", "delete", "get", "head", "options", "patch", "post",
            "put", "trace" };
    private static final String VARIABLE_PREFIX = ":";
    private static final String BLANK = "";

    public static Map<String, Set<String>> findAllHttpRequests(CompilationUnitTree unit) {
        final String source = unit.getSourceName()
            .substring(0, unit.getSourceName()
                .lastIndexOf(SEPARATOR) + 1);
        final Map<String, String> assignments = findVariableAssignments(unit);
        final Map<String, Set<String>> requests = join(findFunctionCallsWithUrls(unit),
                findFunctionDeclarationsWithUrls(unit), findDirectHttpRequest(unit));
        final Map<String, Set<String>> normalizedRequests = new HashMap<>();
        for (final String key : requests.keySet()) {
            final Set<String> resolvedUrls = new HashSet<>();
            for (final String url : requests.get(key)) {
                if (url.startsWith(VARIABLE_PREFIX)) {
                    if (assignments.containsKey(url)) {
                        resolvedUrls.add(assignments.get(url));
                    }
                } else {
                    resolvedUrls.add(url);
                }
            }
            normalizedRequests.put(source + key.replaceAll(START_NONWORD_CHARS, BLANK), resolvedUrls);
        }

        return normalizedRequests;
    }

    public static Map<String, Set<String>> findDirectHttpRequest(Tree element) {
        final Map<String, Set<String>> calls = new HashMap<>();
        element.accept(new SimpleTreeVisitorES6<Void, Void>() {
            @Override
            public Void visitFunctionCall(final FunctionCallTree node, final Void v) {
                if (((node.getFunctionSelect() instanceof final MemberSelectTree member)
                        && (Arrays.stream(HTTP_REQUESTS)
                            .filter(r -> r.equalsIgnoreCase(member.getIdentifier()))
                            .findAny()
                            .isPresent())
                        && (member.getExpression() instanceof final IdentifierTree identifier && identifier.getName()
                            .toLowerCase()
                            .contains(HTTP_KEYWORD)))) {
                    final String caller = identifier.getName() + SEPARATOR + member.getIdentifier();
                    final Set<String> urls = findLiteralsInArguments(node.getArguments());
                    if (!urls.isEmpty()) {
                        calls.put(caller, urls);
                    }
                }
                return super.visitFunctionCall(node, null);
            }
        }, null);
        return calls;
    }

    public static Map<String, Set<String>> findFunctionCallsWithUrls(Tree element) {
        final Map<String, Set<String>> calls = new HashMap<>();
        element.accept(new SimpleTreeVisitorES6<Void, Void>() {
            @Override
            public Void visitFunctionCall(FunctionCallTree functionCallTree, Void v) {
                final Set<String> urls = new HashSet<>();
                String literalValue = BLANK;
                for (final ExpressionTree argument : functionCallTree.getArguments()) {
                    urls.addAll(findLiteralsForIdentifier(argument, URL_KEYWORD));
                }
                if (urls.isEmpty()) {
                    return null;
                }
                if ((functionCallTree.getFunctionSelect() instanceof final MemberSelectTree member)
                        && (member.getExpression() instanceof final FunctionCallTree call)) {
                    if (call.getArguments()
                        .isEmpty()) {
                        return super.visitFunctionCall(functionCallTree, null);
                    }
                    if (call.getArguments()
                        .get(0) instanceof final LiteralTree literal) {
                        literalValue = String.valueOf(literal.getValue());
                    }
                }
                if (!literalValue.isBlank()) {
                    calls.put(literalValue, urls);
                }
                return super.visitFunctionCall(functionCallTree, null);
            }
        }, null);
        return calls;
    }

    public static Map<String, Set<String>> findFunctionDeclarationsWithUrls(Tree element) {
        final Map<String, Set<String>> declarations = new HashMap<>();
        element.accept(new SimpleTreeVisitorES6<Void, Void>() {
            @Override
            public Void visitFunctionDeclaration(FunctionDeclarationTree functionDeclaration, Void v) {
                final Set<String> urls = findLiteralsForIdentifier(functionDeclaration, URL_KEYWORD);
                if (!urls.isEmpty()) {
                    declarations.put(functionDeclaration.getName()
                        .getName(), urls);
                }
                return super.visitFunctionDeclaration(functionDeclaration, null);
            }
        }, null);
        return declarations;
    }

    private static String findLiteralInExpression(ExpressionTree expression) {
        if (expression != null) {
            if (expression instanceof final LiteralTree literal) {
                return String.valueOf(literal.getValue());
            }
            if (expression instanceof final IdentifierTree identifier) {
                return VARIABLE_PREFIX + String.valueOf(identifier.getName());
            }
            if (expression instanceof final MemberSelectTree member) {
                return VARIABLE_PREFIX + String.valueOf(member.getIdentifier());
            }
            if (expression instanceof final BinaryTree binary) {
                return findLiteralInExpression(binary.getLeftOperand())
                        + findLiteralInExpression(binary.getRightOperand());
            }
        }
        return BLANK;
    }

    public static Set<String> findLiteralsForIdentifier(Tree element, String identifier) {
        final Set<String> literals = new HashSet<>();
        element.accept(new SimpleTreeVisitorES6<Void, Void>() {
            @Override
            public Void visitObjectLiteral(ObjectLiteralTree objectLiteral, Void v) {
                for (final PropertyTree property : objectLiteral.getProperties()) {
                    if (((property.getKey() instanceof final IdentifierTree id)
                            && identifier.equalsIgnoreCase(id.getName()))) {
                        literals.add(findLiteralInExpression(property.getValue()));
                        return super.visitObjectLiteral(objectLiteral, null);
                    }
                }
                return super.visitObjectLiteral(objectLiteral, null);
            }
        }, null);
        return literals;
    }

    private static Set<String> findLiteralsInArguments(final List<? extends ExpressionTree> arguments) {
        final Set<String> urls = new HashSet<>();
        for (final ExpressionTree argument : arguments) {
            final String url = findLiteralInExpression(argument);
            if (!url.isBlank()) {
                urls.add(url);
            }
        }
        return urls;
    }

    public static Map<String, String> findVariableAssignments(Tree element) {
        final Map<String, String> assignments = new HashMap<>();
        element.accept(new SimpleTreeVisitorES6<Void, Void>() {
            @Override
            public Void visitVariable(VariableTree node, Void v) {
                String id = BLANK;
                if (node.getBinding() instanceof final MemberSelectTree member) {
                    id = member.getIdentifier();
                } else if (node.getBinding() instanceof final IdentifierTree identifier) {
                    id = identifier.getName();
                }
                final String url = findLiteralInExpression(node.getInitializer());
                if (!id.isBlank() && !url.isBlank()) {
                    assignments.put(VARIABLE_PREFIX + id, url);
                }
                return super.visitVariable(node, null);
            }
        }, null);
        return assignments;
    }

    @SafeVarargs
    public static Map<String, Set<String>> join(Map<String, Set<String>>... maps) {
        final Map<String, Set<String>> join = new HashMap<>();
        for (final Map<String, Set<String>> map : maps) {
            for (final String key : map.keySet()) {
                if (join.containsKey(key)) {
                    join.get(key)
                        .addAll(map.get(key));
                } else {
                    join.put(key, map.get(key));
                }
            }
        }
        return join;
    }

    public static CompilationUnitTree parse(String pathname) {
        try {
            return Parser.create()
                .parse(new File(pathname), d -> {
                    System.out.println(d);
                });
        } catch (NashornException | IOException e) {
            System.out.println(e);
        }
        return null;
    }

    private EcmaScript() {
    }

}
