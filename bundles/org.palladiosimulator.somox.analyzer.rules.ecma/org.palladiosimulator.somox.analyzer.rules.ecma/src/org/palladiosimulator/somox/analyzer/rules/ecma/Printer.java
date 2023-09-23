package org.palladiosimulator.somox.analyzer.rules.ecma;

import org.openjdk.nashorn.api.tree.ArrayAccessTree;
import org.openjdk.nashorn.api.tree.ArrayLiteralTree;
import org.openjdk.nashorn.api.tree.AssignmentTree;
import org.openjdk.nashorn.api.tree.BinaryTree;
import org.openjdk.nashorn.api.tree.BlockTree;
import org.openjdk.nashorn.api.tree.BreakTree;
import org.openjdk.nashorn.api.tree.CaseTree;
import org.openjdk.nashorn.api.tree.CatchTree;
import org.openjdk.nashorn.api.tree.ClassDeclarationTree;
import org.openjdk.nashorn.api.tree.ClassExpressionTree;
import org.openjdk.nashorn.api.tree.CompilationUnitTree;
import org.openjdk.nashorn.api.tree.CompoundAssignmentTree;
import org.openjdk.nashorn.api.tree.ConditionalExpressionTree;
import org.openjdk.nashorn.api.tree.ContinueTree;
import org.openjdk.nashorn.api.tree.DebuggerTree;
import org.openjdk.nashorn.api.tree.DoWhileLoopTree;
import org.openjdk.nashorn.api.tree.EmptyStatementTree;
import org.openjdk.nashorn.api.tree.ErroneousTree;
import org.openjdk.nashorn.api.tree.ExportEntryTree;
import org.openjdk.nashorn.api.tree.ExpressionStatementTree;
import org.openjdk.nashorn.api.tree.ForInLoopTree;
import org.openjdk.nashorn.api.tree.ForLoopTree;
import org.openjdk.nashorn.api.tree.ForOfLoopTree;
import org.openjdk.nashorn.api.tree.FunctionCallTree;
import org.openjdk.nashorn.api.tree.FunctionDeclarationTree;
import org.openjdk.nashorn.api.tree.FunctionExpressionTree;
import org.openjdk.nashorn.api.tree.IdentifierTree;
import org.openjdk.nashorn.api.tree.IfTree;
import org.openjdk.nashorn.api.tree.ImportEntryTree;
import org.openjdk.nashorn.api.tree.InstanceOfTree;
import org.openjdk.nashorn.api.tree.LabeledStatementTree;
import org.openjdk.nashorn.api.tree.LiteralTree;
import org.openjdk.nashorn.api.tree.MemberSelectTree;
import org.openjdk.nashorn.api.tree.ModuleTree;
import org.openjdk.nashorn.api.tree.NewTree;
import org.openjdk.nashorn.api.tree.ObjectLiteralTree;
import org.openjdk.nashorn.api.tree.ParenthesizedTree;
import org.openjdk.nashorn.api.tree.PropertyTree;
import org.openjdk.nashorn.api.tree.RegExpLiteralTree;
import org.openjdk.nashorn.api.tree.ReturnTree;
import org.openjdk.nashorn.api.tree.SimpleTreeVisitorES6;
import org.openjdk.nashorn.api.tree.SpreadTree;
import org.openjdk.nashorn.api.tree.SwitchTree;
import org.openjdk.nashorn.api.tree.TemplateLiteralTree;
import org.openjdk.nashorn.api.tree.ThrowTree;
import org.openjdk.nashorn.api.tree.Tree;
import org.openjdk.nashorn.api.tree.TryTree;
import org.openjdk.nashorn.api.tree.UnaryTree;
import org.openjdk.nashorn.api.tree.VariableTree;
import org.openjdk.nashorn.api.tree.WhileLoopTree;
import org.openjdk.nashorn.api.tree.WithTree;
import org.openjdk.nashorn.api.tree.YieldTree;

public final class Printer extends SimpleTreeVisitorES6<Void, Void> {
	private CompilationUnitTree unit;

	private void print(Tree node) {
		if (node.equals(this.unit)) {
		} else if (node instanceof final ArrayLiteralTree array) {
			array.getElements();
		} else if (node instanceof final AssignmentTree assignment) {
			assignment.getVariable();
		} else if (node instanceof final BlockTree block) {
			block.getStatements();
		} else if (node instanceof final CompilationUnitTree compilation) {
			System.out.println(node.getClass().getSimpleName() + ": " + compilation.getSourceName());
		} else if (node instanceof final ExpressionStatementTree expression) {
			expression.getExpression();
		} else if (node instanceof final FunctionCallTree function) {
			function.getFunctionSelect();
		} else if (node instanceof final FunctionExpressionTree function) {
			final String name = function.getName() == null ? "null" : String.valueOf(function.getName().getName());
			System.out.println(node.getClass().getSimpleName() + ": " + name);
		} else if (node instanceof final IdentifierTree identifier) {
			System.out.println(node.getClass().getSimpleName() + ": " + identifier.getName());
		} else if (node instanceof final LiteralTree literal) {
			System.out.println(node.getClass().getSimpleName() + ": " + String.valueOf(literal.getValue()));
		} else if (node instanceof final MemberSelectTree member) {
			System.out.println(node.getClass().getSimpleName() + ": " + member.getIdentifier());
		} else if (node instanceof final VariableTree variable) {
			variable.getBinding();
		} else {
			System.out.println(node);
		}
	}

	@Override
	public Void visitArrayAccess(final ArrayAccessTree node, final Void v) {
		this.print(node);
		return super.visitArrayAccess(node, null);
	}

	@Override
	public Void visitArrayLiteral(final ArrayLiteralTree node, final Void v) {
		this.print(node);
		return super.visitArrayLiteral(node, null);
	}

	@Override
	public Void visitAssignment(final AssignmentTree node, final Void v) {
		this.print(node);
		return super.visitAssignment(node, null);
	}

	@Override
	public Void visitBinary(final BinaryTree node, final Void v) {
		this.print(node);
		return super.visitBinary(node, null);
	}

	@Override
	public Void visitBlock(final BlockTree node, final Void v) {
		this.print(node);
		return super.visitBlock(node, null);
	}

	@Override
	public Void visitBreak(final BreakTree node, final Void v) {
		this.print(node);
		return super.visitBreak(node, null);
	}

	@Override
	public Void visitCase(final CaseTree node, final Void v) {
		this.print(node);
		return super.visitCase(node, null);
	}

	@Override
	public Void visitCatch(final CatchTree node, final Void v) {
		this.print(node);
		return super.visitCatch(node, null);
	}

	@Override
	public Void visitClassDeclaration(final ClassDeclarationTree node, final Void v) {
		this.print(node);
		return super.visitClassDeclaration(node, null);
	}

	@Override
	public Void visitClassExpression(final ClassExpressionTree node, final Void v) {
		this.print(node);
		return super.visitClassExpression(node, null);
	}

	@Override
	public Void visitCompilationUnit(final CompilationUnitTree node, final Void v) {
		this.unit = node;
		this.print(node);
		return super.visitCompilationUnit(node, null);
	}

	@Override
	public Void visitCompoundAssignment(final CompoundAssignmentTree node, final Void v) {
		this.print(node);
		return super.visitCompoundAssignment(node, null);
	}

	@Override
	public Void visitConditionalExpression(final ConditionalExpressionTree node, final Void v) {
		this.print(node);
		return super.visitConditionalExpression(node, null);
	}

	@Override
	public Void visitContinue(final ContinueTree node, final Void v) {
		this.print(node);
		return super.visitContinue(node, null);
	}

	@Override
	public Void visitDebugger(final DebuggerTree node, final Void v) {
		this.print(node);
		return super.visitDebugger(node, null);
	}

	@Override
	public Void visitDoWhileLoop(final DoWhileLoopTree node, final Void v) {
		this.print(node);
		return super.visitDoWhileLoop(node, null);
	}

	@Override
	public Void visitEmptyStatement(final EmptyStatementTree node, final Void v) {
		this.print(node);
		return super.visitEmptyStatement(node, null);
	}

	@Override
	public Void visitErroneous(final ErroneousTree node, final Void v) {
		this.print(node);
		return super.visitErroneous(node, null);
	}

	@Override
	public Void visitExportEntry(final ExportEntryTree node, final Void v) {
		this.print(node);
		return super.visitExportEntry(node, null);
	}

	@Override
	public Void visitExpressionStatement(final ExpressionStatementTree node, final Void v) {
		this.print(node);
		return super.visitExpressionStatement(node, null);
	}

	@Override
	public Void visitForInLoop(final ForInLoopTree node, final Void v) {
		this.print(node);
		return super.visitForInLoop(node, null);
	}

	@Override
	public Void visitForLoop(final ForLoopTree node, final Void v) {
		this.print(node);
		return super.visitForLoop(node, null);
	}

	@Override
	public Void visitForOfLoop(final ForOfLoopTree node, final Void v) {
		this.print(node);
		return super.visitForOfLoop(node, null);
	}

	@Override
	public Void visitFunctionCall(final FunctionCallTree node, final Void v) {
		this.print(node);
		return super.visitFunctionCall(node, null);
	}

	@Override
	public Void visitFunctionDeclaration(final FunctionDeclarationTree node, final Void v) {
		this.print(node);
		return super.visitFunctionDeclaration(node, null);
	}

	@Override
	public Void visitFunctionExpression(final FunctionExpressionTree node, final Void v) {
		this.print(node);
		return super.visitFunctionExpression(node, null);
	}

	@Override
	public Void visitIdentifier(final IdentifierTree node, final Void v) {
		this.print(node);
		return super.visitIdentifier(node, null);
	}

	@Override
	public Void visitIf(final IfTree node, final Void v) {
		this.print(node);
		return super.visitIf(node, null);
	}

	@Override
	public Void visitImportEntry(final ImportEntryTree node, final Void v) {
		this.print(node);
		return super.visitImportEntry(node, null);
	}

	@Override
	public Void visitInstanceOf(final InstanceOfTree node, final Void v) {
		this.print(node);
		return super.visitInstanceOf(node, null);
	}

	@Override
	public Void visitLabeledStatement(final LabeledStatementTree node, final Void v) {
		this.print(node);
		return super.visitLabeledStatement(node, null);
	}

	@Override
	public Void visitLiteral(final LiteralTree node, final Void v) {
		this.print(node);
		return super.visitLiteral(node, null);
	}

	@Override
	public Void visitMemberSelect(final MemberSelectTree node, final Void v) {
		this.print(node);
		return super.visitMemberSelect(node, null);
	}

	@Override
	public Void visitModule(final ModuleTree node, final Void v) {
		this.print(node);
		return super.visitModule(node, null);
	}

	@Override
	public Void visitNew(final NewTree node, final Void v) {
		this.print(node);
		return super.visitNew(node, null);
	}

	@Override
	public Void visitObjectLiteral(final ObjectLiteralTree node, final Void v) {
		this.print(node);
		return super.visitObjectLiteral(node, null);
	}

	@Override
	public Void visitParenthesized(final ParenthesizedTree node, final Void v) {
		this.print(node);
		return super.visitParenthesized(node, null);
	}

	@Override
	public Void visitProperty(final PropertyTree node, final Void v) {
		this.print(node);
		return super.visitProperty(node, null);
	}

	@Override
	public Void visitRegExpLiteral(final RegExpLiteralTree node, final Void v) {
		this.print(node);
		return super.visitRegExpLiteral(node, null);
	}

	@Override
	public Void visitReturn(final ReturnTree node, final Void v) {
		this.print(node);
		return super.visitReturn(node, null);
	}

	@Override
	public Void visitSpread(final SpreadTree node, final Void v) {
		this.print(node);
		return super.visitSpread(node, null);
	}

	@Override
	public Void visitSwitch(final SwitchTree node, final Void v) {
		this.print(node);
		return super.visitSwitch(node, null);
	}

	@Override
	public Void visitTemplateLiteral(final TemplateLiteralTree node, final Void v) {
		this.print(node);
		return super.visitTemplateLiteral(node, null);
	}

	@Override
	public Void visitThrow(final ThrowTree node, final Void v) {
		this.print(node);
		return super.visitThrow(node, null);
	}

	@Override
	public Void visitTry(final TryTree node, final Void v) {
		this.print(node);
		return super.visitTry(node, null);
	}

	@Override
	public Void visitUnary(final UnaryTree node, final Void v) {
		this.print(node);
		return super.visitUnary(node, null);
	}

	@Override
	public Void visitUnknown(final Tree node, final Void v) {
		this.print(node);
		return super.visitUnknown(node, null);
	}

	@Override
	public Void visitVariable(final VariableTree node, final Void v) {
		this.print(node);
		return super.visitVariable(node, null);
	}

	@Override
	public Void visitWhileLoop(final WhileLoopTree node, final Void v) {
		this.print(node);
		return super.visitWhileLoop(node, null);
	}

	@Override
	public Void visitWith(final WithTree node, final Void v) {
		this.print(node);
		return super.visitWith(node, null);
	}

	@Override
	public Void visitYield(final YieldTree node, final Void v) {
		this.print(node);
		return super.visitYield(node, null);
	}
}
