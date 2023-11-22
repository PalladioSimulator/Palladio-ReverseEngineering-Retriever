package org.palladiosimulator.somox.analyzer.rules.test.workflow.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public final class MethodDeclarationVisitor extends ASTVisitor {
    private final List<MethodDeclaration> declarations = new ArrayList<>();

    public static List<MethodDeclaration> perform(ASTNode node) {
        MethodDeclarationVisitor visitor = new MethodDeclarationVisitor();
        node.accept(visitor);
        return visitor.getMethods();
    }

    @Override
    public boolean visit(final MethodDeclaration method) {
        declarations.add(method);
        return super.visit(method);
    }

    public List<MethodDeclaration> getMethods() {
        return Collections.unmodifiableList(declarations);
    }
}
