package org.palladiosimulator.somox.analyzer.rules.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.palladiosimulator.somox.analyzer.rules.model.CompUnitOrName;
import org.palladiosimulator.somox.analyzer.rules.model.Component;
import org.palladiosimulator.somox.analyzer.rules.model.ComponentBuilder;
import org.palladiosimulator.somox.analyzer.rules.model.Composite;
import org.palladiosimulator.somox.analyzer.rules.model.CompositeBuilder;
import org.palladiosimulator.somox.analyzer.rules.model.EntireInterface;
import org.palladiosimulator.somox.analyzer.rules.model.JavaInterfaceName;
import org.palladiosimulator.somox.analyzer.rules.model.JavaOperationName;
import org.palladiosimulator.somox.analyzer.rules.model.Operation;
import org.palladiosimulator.somox.analyzer.rules.model.OperationInterface;
import org.palladiosimulator.somox.analyzer.rules.model.Provisions;
import org.palladiosimulator.somox.analyzer.rules.model.Requirements;

public class CompositeTest {

    @Test
    void emptyComposite() {
        CompositeBuilder compositeBuilder = new CompositeBuilder("CompositeComponent");
        Composite result = compositeBuilder.construct(List.of(), new Requirements(List.of(), List.of(), List.of()),
                new Provisions(List.of(), List.of()), List.of());

        assertTrue(result.parts()
            .isEmpty(), "empty composite should have no parts");
        assertTrue(result.requirements()
            .isEmpty(), "empty composite should have no requirements");
        assertTrue(result.provisions()
            .isEmpty(), "empty composite should have no provisions");
    }

    @Test
    void singletonComposite() {
        OperationInterface provision = new Operation(null, new JavaOperationName("Interface", "providedMethod"));
        EntireInterface requirement = new EntireInterface(new JavaInterfaceName("RequiredInterface"));

        ComponentBuilder componentBuilder = new ComponentBuilder(new CompUnitOrName("Component"));
        componentBuilder.provisions()
            .add(provision);
        componentBuilder.requirements()
            .add(requirement);

        CompositeBuilder compositeBuilder = new CompositeBuilder("CompositeComponent");
        compositeBuilder.addPart(componentBuilder);

        List<OperationInterface> allDependencies = List.of(provision, requirement);
        List<OperationInterface> visibleProvisions = List.of(provision);

        Composite result = compositeBuilder.construct(List.of(),
                new Requirements(List.of(), allDependencies, visibleProvisions),
                new Provisions(List.of(), allDependencies), visibleProvisions);

        assertEquals(1, result.parts()
            .size(), "this composite should have exactly one part");
        assertTrue(result.requirements()
            .isEmpty(), "this composite should not have requirements");
        assertTrue(result.provisions()
            .isEmpty(), "this composite should not have provisions");

        Component component = result.parts()
            .stream()
            .findFirst()
            .get();
        assertEquals(1, component.provisions()
            .get()
            .size(), "this component should only have one provision");
        assertEquals(1, component.requirements()
            .get()
            .size(), "this component should only have one requirement");
    }

    @Test
    void exposingSingletonComposite() {
        OperationInterface provision = new Operation(null, new JavaOperationName("Interface", "providedMethod"));
        EntireInterface requirement = new EntireInterface(new JavaInterfaceName("RequiredInterface"));

        ComponentBuilder componentBuilder = new ComponentBuilder(new CompUnitOrName("Component"));
        componentBuilder.provisions()
            .add(provision);
        componentBuilder.requirements()
            .add(requirement);

        CompositeBuilder compositeBuilder = new CompositeBuilder("CompositeComponent");
        compositeBuilder.addPart(componentBuilder);

        List<OperationInterface> allDependencies = List.of(provision, requirement);
        List<OperationInterface> visibleProvisions = List.of(provision);

        Composite result = compositeBuilder.construct(List.of(),
                new Requirements(List.of(requirement), allDependencies, visibleProvisions),
                new Provisions(List.of(provision), allDependencies), visibleProvisions);

        assertEquals(1, result.parts()
            .size(), "this composite should have exactly one part");
        assertEquals(1, result.requirements()
            .size(), "this composite should have exactly one requirement");
        assertEquals(1, result.provisions()
            .size(), "this composite should have exactly one provision");
    }

    @Test
    void twoComponentComposite() {
        OperationInterface provision1 = new Operation(null, new JavaOperationName("InterfaceA", "providedMethodA"));
        OperationInterface provision2 = new Operation(null, new JavaOperationName("InterfaceB", "providedMethodB"));
        EntireInterface requirement1 = new EntireInterface(new JavaInterfaceName("RequiredInterfaceA"));
        EntireInterface requirement2 = new EntireInterface(new JavaInterfaceName("RequiredInterfaceB"));

        ComponentBuilder componentBuilder1 = new ComponentBuilder(new CompUnitOrName("Component 1"));
        componentBuilder1.provisions()
            .add(provision1);
        componentBuilder1.requirements()
            .add(requirement1);

        ComponentBuilder componentBuilder2 = new ComponentBuilder(new CompUnitOrName("Component 2"));
        componentBuilder2.provisions()
            .add(provision2);
        componentBuilder2.requirements()
            .add(requirement2);

        CompositeBuilder compositeBuilder = new CompositeBuilder("CompositeComponent");
        compositeBuilder.addPart(componentBuilder1);
        compositeBuilder.addPart(componentBuilder2);

        List<OperationInterface> allDependencies = List.of(provision1, provision2, requirement1, requirement2);
        List<OperationInterface> visibleProvisions = List.of(provision1, provision2);

        Composite result = compositeBuilder.construct(List.of(),
                new Requirements(List.of(requirement1, requirement2), allDependencies, visibleProvisions),
                new Provisions(List.of(provision1, provision2), allDependencies), visibleProvisions);

        assertEquals(2, result.parts()
            .size(), "this composite should have exactly two parts");
        assertEquals(2, result.requirements()
            .size(), "this composite should have exactly two requirements");
        assertEquals(2, result.provisions()
            .size(), "this composite should have exactly two provisions");
    }

    @Test
    void overlappingTwoComponentComposite() {
        OperationInterface provision = new Operation(null, new JavaOperationName("Interface", "providedMethod"));
        EntireInterface requirement = new EntireInterface(new JavaInterfaceName("RequiredInterface"));
        EntireInterface additionalRequirement1 = new EntireInterface(new JavaInterfaceName("RequiredInterfaceA"));
        EntireInterface additionalRequirement2 = new EntireInterface(new JavaInterfaceName("RequiredInterfaceB"));

        ComponentBuilder componentBuilder1 = new ComponentBuilder(new CompUnitOrName("Component 1"));
        componentBuilder1.provisions()
            .add(provision);
        componentBuilder1.requirements()
            .add(requirement);
        componentBuilder1.requirements()
            .add(additionalRequirement1);

        ComponentBuilder componentBuilder2 = new ComponentBuilder(new CompUnitOrName("Component 2"));
        componentBuilder2.provisions()
            .add(provision);
        componentBuilder2.requirements()
            .add(requirement);
        componentBuilder2.requirements()
            .add(additionalRequirement2);

        CompositeBuilder compositeBuilder = new CompositeBuilder("CompositeComponent");
        compositeBuilder.addPart(componentBuilder1);
        compositeBuilder.addPart(componentBuilder2);

        List<OperationInterface> allDependencies = List.of(provision, requirement, additionalRequirement1,
                additionalRequirement2);
        List<OperationInterface> visibleProvisions = List.of(provision);

        Composite result = compositeBuilder.construct(List.of(),
                new Requirements(List.of(requirement), allDependencies, visibleProvisions),
                new Provisions(List.of(provision), allDependencies), visibleProvisions);

        assertEquals(2, result.parts()
            .size(), "this composite should have exactly two parts");
        assertEquals(1, result.requirements()
            .size(), "this composite should have exactly one requirement");
        assertEquals(1, result.provisions()
            .size(), "this composite should have exactly one provision");
    }

    @Test
    void impreciseExposure() {
        // TODO: Re-think this test.
        OperationInterface provision = new Operation(null, new JavaOperationName("Interface", "providedMethod"));
        OperationInterface impreciseProvision = new EntireInterface(new JavaInterfaceName("Interface"));

        ComponentBuilder componentBuilder = new ComponentBuilder(new CompUnitOrName("Component"));
        componentBuilder.provisions()
            .add(provision);

        CompositeBuilder compositeBuilder = new CompositeBuilder("CompositeComponent");
        compositeBuilder.addPart(componentBuilder);

        List<OperationInterface> allDependencies = List.of(provision);
        List<OperationInterface> visibleProvisions = List.of(provision);

        Composite result = compositeBuilder.construct(List.of(),
                new Requirements(List.of(), allDependencies, visibleProvisions),
                new Provisions(List.of(impreciseProvision), allDependencies), visibleProvisions);

        assertEquals(1, result.parts()
            .size(), "this composite should have exactly one part");
        assertEquals(1, result.provisions()
            .size(), "this composite should have exactly one provision");
    }
}
