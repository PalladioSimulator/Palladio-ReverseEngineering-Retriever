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
import org.palladiosimulator.somox.analyzer.rules.blackboard.CompilationUnitWrapper;
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;

public class SeffAssociationTest extends RuleEngineTest {

    SeffAssociationTest() {
        super("SpringProject", DefaultRule.SPRING);
    }

    @Test
    void allAssociationsReferToExistingMethods() {
        RuleEngineBlackboard blackboard = getBlackboard();
        Map<ASTNode, ServiceEffectSpecification> associations = blackboard.getSeffAssociations();

        for (Map.Entry<ASTNode, ServiceEffectSpecification> association : associations.entrySet()) {
            ASTNode astNode = association.getKey();
            ServiceEffectSpecification seff = association.getValue();
            assertTrue(astNode instanceof MethodDeclaration,
                    "All ASTNodes in the SEFF/AST associations must be MethodDeclarations");

            MethodDeclaration methodDeclaration = (MethodDeclaration) astNode;
            String declarationName = methodDeclaration.getName()
                .getFullyQualifiedName();
            String seffName = seff.getDescribedService__SEFF()
                .getEntityName();
            // SEFF names may have a "$N" suffix after the signature name, where N is a positive
            // integer.
            // This avoids name collisions that can occur because signature identifiers are global.
            assertTrue(seffName.startsWith(declarationName), "SEFF's name must begin with its method's name");
        }
    }

    @Override
    @Test
    void test() {
        RuleEngineBlackboard blackboard = getBlackboard();

        @SuppressWarnings("unchecked")
        MethodDeclaration constructorDeclaration = blackboard.getCompilationUnits()
            .stream()
            .filter(CompilationUnitWrapper::isEclipseCompilationUnit)
            .map(CompilationUnitWrapper::getEclipseCompilationUnit)
            .flatMap(unit -> ((List<ASTNode>) unit.types()).stream())
            .filter(TypeDeclaration.class::isInstance)
            .map(TypeDeclaration.class::cast)
            .filter(type -> type.getName()
                .getFullyQualifiedName()
                .equals("AComponent"))
            .flatMap(type -> Stream.of(type.getMethods()))
            .filter(method -> method.getName()
                .getFullyQualifiedName()
                .equals("AComponent"))
            .findAny()
            .orElseGet(() -> fail("Constructor of AComponent must be present in AST"));

        Map<ASTNode, ServiceEffectSpecification> associations = blackboard.getSeffAssociations();

        // Assume that the SPRING rule detects constructors of components.
        assertTrue(associations.containsKey(constructorDeclaration),
                "SEFF/AST associations must contain all rule-detected methods");
    }
}
