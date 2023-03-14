package org.palladiosimulator.somox.analyzer.rules.test.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.Test;
import org.palladiosimulator.pcm.seff.ServiceEffectSpecification;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;
import org.palladiosimulator.somox.analyzer.rules.blackboard.CompilationUnitWrapper;
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;

public class SeffAssociationTest extends RuleEngineTest {

    SeffAssociationTest() {
        super("external/piggymetrics-spring.version.2.0.3", DefaultRule.SPRING);
    }

    @Test
    void test() {
        RuleEngineBlackboard blackboard = getBlackboard();
        Map<ASTNode, ServiceEffectSpecification> associations = blackboard.getSeffAssociations();
        Set<CompilationUnit> compilationUnits = blackboard.getCompilationUnits()
            .stream()
            .filter(CompilationUnitWrapper::isEclipseCompilationUnit)
            .map(CompilationUnitWrapper::getEclipseCompilationUnit)
            .collect(Collectors.toSet());

        for (Map.Entry<ASTNode, ServiceEffectSpecification> association : associations.entrySet()) {
            ASTNode astNode = association.getKey();
            ServiceEffectSpecification seff = association.getValue();
            assertTrue(astNode instanceof MethodDeclaration);

            MethodDeclaration methodDeclaration = (MethodDeclaration) astNode;
            String declarationName = methodDeclaration.getName()
                .getFullyQualifiedName();
            String seffName = seff.getDescribedService__SEFF()
                .getEntityName();
            // SEFF names may have a "$N" suffix after the signature name, where N is a positive
            // integer.
            // This avoids name collisions that can occur because signature identifiers are global.
            assertTrue(seffName.startsWith(declarationName));
        }
    }
}
