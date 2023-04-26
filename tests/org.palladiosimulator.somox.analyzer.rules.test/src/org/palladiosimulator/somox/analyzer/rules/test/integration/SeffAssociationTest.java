package org.palladiosimulator.somox.analyzer.rules.test.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.jupiter.api.Test;
import org.palladiosimulator.pcm.seff.ServiceEffectSpecification;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;

public class SeffAssociationTest extends RuleEngineTest {

    SeffAssociationTest() {
        super("SpringProject", DefaultRule.SPRING);
    }

    @Test
    void allAssociationsReferToMethods() {
        RuleEngineBlackboard blackboard = getBlackboard();
        Map<ASTNode, ServiceEffectSpecification> associations = blackboard.getSeffAssociations();

        for (Map.Entry<ASTNode, ServiceEffectSpecification> association : associations.entrySet()) {
            ASTNode astNode = association.getKey();
            assertTrue(astNode instanceof MethodDeclaration,
                    "All ASTNodes in the SEFF/AST associations must be MethodDeclarations");
            // SEFF names may have a "$N" suffix after the signature name, where N is a positive
            // integer.
            // This avoids name collisions that can occur because signature identifiers are global.

            // The AST node's name may be a prefix of the SEFF name, but it does not have to be.
            // Alternatively, the SEFF name may begin with the (REST) path that that method is
            // mapped to.
        }
    }

    @Override
    @Test
    void test() {
        RuleEngineBlackboard blackboard = getBlackboard();

        @SuppressWarnings("unchecked")
        MethodDeclaration methodDeclaration = blackboard.getCompilationUnits()
            .stream()
            .flatMap(unit -> ((List<ASTNode>) unit.types()).stream())
            .filter(TypeDeclaration.class::isInstance)
            .map(TypeDeclaration.class::cast)
            .filter(type -> type.getName()
                .getFullyQualifiedName()
                .equals("AController"))
            .flatMap(type -> Stream.of(type.getMethods()))
            .filter(method -> method.getName()
                .getFullyQualifiedName()
                .equals("aMethod"))
            .findAny()
            .orElseGet(() -> fail("AController::aMethod must be present in AST"));

        Map<ASTNode, ServiceEffectSpecification> associations = blackboard.getSeffAssociations();

        // Assume that the SPRING rule detects constructors of components.
        assertTrue(associations.containsKey(methodDeclaration),
                "SEFF/AST associations must contain all rule-detected methods");
    }
}
