package org.palladiosimulator.retriever.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.palladiosimulator.retriever.extraction.commonalities.CompUnitOrName;
import org.palladiosimulator.retriever.extraction.commonalities.Component;
import org.palladiosimulator.retriever.extraction.commonalities.ComponentBuilder;
import org.palladiosimulator.retriever.extraction.commonalities.Composite;
import org.palladiosimulator.retriever.extraction.commonalities.CompositeBuilder;
import org.palladiosimulator.retriever.extraction.commonalities.EntireInterface;
import org.palladiosimulator.retriever.extraction.commonalities.JavaInterfaceName;
import org.palladiosimulator.retriever.extraction.commonalities.JavaOperationName;
import org.palladiosimulator.retriever.extraction.commonalities.Operation;
import org.palladiosimulator.retriever.extraction.commonalities.OperationInterface;
import org.palladiosimulator.retriever.extraction.commonalities.Provisions;
import org.palladiosimulator.retriever.extraction.commonalities.Requirements;

public class CompositeTest {

    @Test
    void emptyComposite() {
        final CompositeBuilder compositeBuilder = new CompositeBuilder("CompositeComponent");
        final Composite result = compositeBuilder.construct(List.of(),
                new Requirements(List.of(), List.of(), List.of()), new Provisions(List.of(), List.of()), List.of());

        assertTrue(result.parts()
            .isEmpty(), "empty composite should have no parts");
        assertTrue(result.requirements()
            .isEmpty(), "empty composite should have no requirements");
        assertTrue(result.provisions()
            .isEmpty(), "empty composite should have no provisions");
    }

    @Test
    void singletonComposite() {
        final OperationInterface provision = new Operation(null, new JavaOperationName("Interface", "providedMethod"));
        final EntireInterface requirement = new EntireInterface(new JavaInterfaceName("RequiredInterface"));

        final ComponentBuilder componentBuilder = new ComponentBuilder(new CompUnitOrName("Component"));
        componentBuilder.provisions()
            .add(provision);
        componentBuilder.requirements()
            .add(requirement);

        final CompositeBuilder compositeBuilder = new CompositeBuilder("CompositeComponent");
        compositeBuilder.addPart(componentBuilder);

        final List<OperationInterface> allDependencies = List.of(provision, requirement);
        final List<OperationInterface> visibleProvisions = List.of(provision);

        final Composite result = compositeBuilder.construct(List.of(),
                new Requirements(List.of(), allDependencies, visibleProvisions),
                new Provisions(List.of(), allDependencies), visibleProvisions);

        assertEquals(1, result.parts()
            .size(), "this composite should have exactly one part");
        assertTrue(result.requirements()
            .isEmpty(), "this composite should not have requirements");
        assertTrue(result.provisions()
            .isEmpty(), "this composite should not have provisions");

        final Component component = result.parts()
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
        final OperationInterface provision = new Operation(null, new JavaOperationName("Interface", "providedMethod"));
        final EntireInterface requirement = new EntireInterface(new JavaInterfaceName("RequiredInterface"));

        final ComponentBuilder componentBuilder = new ComponentBuilder(new CompUnitOrName("Component"));
        componentBuilder.provisions()
            .add(provision);
        componentBuilder.requirements()
            .add(requirement);

        final CompositeBuilder compositeBuilder = new CompositeBuilder("CompositeComponent");
        compositeBuilder.addPart(componentBuilder);

        final List<OperationInterface> allDependencies = List.of(provision, requirement);
        final List<OperationInterface> visibleProvisions = List.of(provision);

        final Composite result = compositeBuilder.construct(List.of(),
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
        final OperationInterface provision1 = new Operation(null,
                new JavaOperationName("InterfaceA", "providedMethodA"));
        final OperationInterface provision2 = new Operation(null,
                new JavaOperationName("InterfaceB", "providedMethodB"));
        final EntireInterface requirement1 = new EntireInterface(new JavaInterfaceName("RequiredInterfaceA"));
        final EntireInterface requirement2 = new EntireInterface(new JavaInterfaceName("RequiredInterfaceB"));

        final ComponentBuilder componentBuilder1 = new ComponentBuilder(new CompUnitOrName("Component 1"));
        componentBuilder1.provisions()
            .add(provision1);
        componentBuilder1.requirements()
            .add(requirement1);

        final ComponentBuilder componentBuilder2 = new ComponentBuilder(new CompUnitOrName("Component 2"));
        componentBuilder2.provisions()
            .add(provision2);
        componentBuilder2.requirements()
            .add(requirement2);

        final CompositeBuilder compositeBuilder = new CompositeBuilder("CompositeComponent");
        compositeBuilder.addPart(componentBuilder1);
        compositeBuilder.addPart(componentBuilder2);

        final List<OperationInterface> allDependencies = List.of(provision1, provision2, requirement1, requirement2);
        final List<OperationInterface> visibleProvisions = List.of(provision1, provision2);

        final Composite result = compositeBuilder.construct(List.of(),
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
        final OperationInterface provision = new Operation(null, new JavaOperationName("Interface", "providedMethod"));
        final EntireInterface requirement = new EntireInterface(new JavaInterfaceName("RequiredInterface"));
        final EntireInterface additionalRequirement1 = new EntireInterface(new JavaInterfaceName("RequiredInterfaceA"));
        final EntireInterface additionalRequirement2 = new EntireInterface(new JavaInterfaceName("RequiredInterfaceB"));

        final ComponentBuilder componentBuilder1 = new ComponentBuilder(new CompUnitOrName("Component 1"));
        componentBuilder1.provisions()
            .add(provision);
        componentBuilder1.requirements()
            .add(requirement);
        componentBuilder1.requirements()
            .add(additionalRequirement1);

        final ComponentBuilder componentBuilder2 = new ComponentBuilder(new CompUnitOrName("Component 2"));
        componentBuilder2.provisions()
            .add(provision);
        componentBuilder2.requirements()
            .add(requirement);
        componentBuilder2.requirements()
            .add(additionalRequirement2);

        final CompositeBuilder compositeBuilder = new CompositeBuilder("CompositeComponent");
        compositeBuilder.addPart(componentBuilder1);
        compositeBuilder.addPart(componentBuilder2);

        final List<OperationInterface> allDependencies = List.of(provision, requirement, additionalRequirement1,
                additionalRequirement2);
        final List<OperationInterface> visibleProvisions = List.of(provision);

        final Composite result = compositeBuilder.construct(List.of(),
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
        final OperationInterface provision = new Operation(null, new JavaOperationName("Interface", "providedMethod"));
        final OperationInterface impreciseProvision = new EntireInterface(new JavaInterfaceName("Interface"));

        final ComponentBuilder componentBuilder = new ComponentBuilder(new CompUnitOrName("Component"));
        componentBuilder.provisions()
            .add(provision);

        final CompositeBuilder compositeBuilder = new CompositeBuilder("CompositeComponent");
        compositeBuilder.addPart(componentBuilder);

        final List<OperationInterface> allDependencies = List.of(provision);
        final List<OperationInterface> visibleProvisions = List.of(provision);

        final Composite result = compositeBuilder.construct(List.of(),
                new Requirements(List.of(), allDependencies, visibleProvisions),
                new Provisions(List.of(impreciseProvision), allDependencies), visibleProvisions);

        assertEquals(1, result.parts()
            .size(), "this composite should have exactly one part");
        assertEquals(1, result.provisions()
            .size(), "this composite should have exactly one provision");
    }
}
