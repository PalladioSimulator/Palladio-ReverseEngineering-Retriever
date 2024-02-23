package org.palladiosimulator.retriever.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.palladiosimulator.retriever.extraction.commonalities.Component;
import org.palladiosimulator.retriever.extraction.commonalities.ComponentBuilder;
import org.palladiosimulator.retriever.extraction.commonalities.EntireInterface;
import org.palladiosimulator.retriever.extraction.commonalities.HTTPMethod;
import org.palladiosimulator.retriever.extraction.commonalities.JavaInterfaceName;
import org.palladiosimulator.retriever.extraction.commonalities.JavaOperationName;
import org.palladiosimulator.retriever.extraction.commonalities.Operation;
import org.palladiosimulator.retriever.extraction.commonalities.OperationInterface;
import org.palladiosimulator.retriever.extraction.commonalities.RESTName;

public class InterfaceTest {

    @Test
    void singleJavaOperation() {
        final ComponentBuilder builder = new ComponentBuilder(null);
        final Operation expectedOperation = new Operation(null, new JavaOperationName("Interface", "method"));
        builder.provisions()
            .add(expectedOperation);

        final List<OperationInterface> allDependencies = List.of(expectedOperation);
        final List<OperationInterface> visibleProvisions = List.of(expectedOperation);

        final Component builtComponent = builder.create(allDependencies, visibleProvisions);
        assertTrue(builtComponent.provisions()
            .containsPartOf(expectedOperation));

        final Map<OperationInterface, SortedSet<Operation>> simplifiedProvisions = builtComponent.provisions()
            .simplified();

        final Set<OperationInterface> interfaces = simplifiedProvisions.keySet();
        assertFalse(interfaces.isEmpty(), "empty result");
        assertEquals(1, interfaces.size(), "more than one interface");
        final OperationInterface commonInterface = interfaces.stream()
            .findFirst()
            .get();
        assertEquals(expectedOperation, commonInterface, "operation does not have the correct interface");

        final SortedSet<Operation> operations = simplifiedProvisions.get(expectedOperation);
        assertEquals(1, operations.size(), "more than one operation in the interface");

        final List<Operation> firstMethodCandidates = operations.stream()
            .filter(x -> Optional.of("method")
                .equals(x.getName()
                    .forInterface("Interface")))
            .collect(Collectors.toList());

        assertFalse(firstMethodCandidates.isEmpty(), "interface does not contain an operation");
        assertEquals(1, firstMethodCandidates.size(), "interface contains multiple instances of the operation");
        assertEquals(1, operations.size(), "interface contains additional operations");
    }

    @Test
    void singlePathOperation() {
        final ComponentBuilder builder = new ComponentBuilder(null);
        final Operation expectedOperation = new Operation(null, new RESTName("test-host", "/method", HTTPMethod.GET));
        builder.provisions()
            .add(expectedOperation);

        final List<OperationInterface> allDependencies = List.of(expectedOperation);
        final List<OperationInterface> visibleProvisions = List.of(expectedOperation);

        final Component builtComponent = builder.create(allDependencies, visibleProvisions);
        assertTrue(builtComponent.provisions()
            .containsPartOf(expectedOperation));

        final Map<OperationInterface, SortedSet<Operation>> simplifiedProvisions = builtComponent.provisions()
            .simplified();

        final Set<OperationInterface> interfaces = simplifiedProvisions.keySet();
        assertFalse(interfaces.isEmpty(), "empty result");
        assertEquals(1, interfaces.size(), "more than one interface");
        final OperationInterface commonInterface = interfaces.stream()
            .findFirst()
            .get();
        assertEquals(expectedOperation, commonInterface, "operation does not have the correct interface");

        final SortedSet<Operation> operations = simplifiedProvisions.get(commonInterface);
        assertEquals(1, operations.size(), "more than one operation in the interface");

        final List<Operation> firstMethodCandidates = operations.stream()
            .filter(x -> Optional.of("test-host/method[GET]")
                .equals(x.getName()
                    .forInterface("test-host/method")))
            .collect(Collectors.toList());

        assertFalse(firstMethodCandidates.isEmpty(), "interface does not contain an operation");
        assertEquals(1, firstMethodCandidates.size(), "interface contains multiple instances of the operation");
        assertEquals(1, operations.size(), "interface contains additional operations");
    }

    @Test
    void entireJavaInterface() {
        final ComponentBuilder builder = new ComponentBuilder(null);
        final Operation firstMethod = new Operation(null, new JavaOperationName("CommonInterface", "firstMethod"));
        final Operation secondMethod = new Operation(null, new JavaOperationName("CommonInterface", "secondMethod"));
        builder.provisions()
            .add(firstMethod);
        builder.provisions()
            .add(secondMethod);

        final List<OperationInterface> allDependencies = List.of(firstMethod, secondMethod);
        final List<OperationInterface> visibleProvisions = List.of(firstMethod, secondMethod);

        final Component builtComponent = builder.create(allDependencies, visibleProvisions);
        final EntireInterface expectedInterface = new EntireInterface(new JavaInterfaceName("CommonInterface"));
        assertTrue(builtComponent.provisions()
            .containsPartOf(expectedInterface));

        final Map<OperationInterface, SortedSet<Operation>> simplifiedProvisions = builtComponent.provisions()
            .simplified();

        final Set<OperationInterface> interfaces = simplifiedProvisions.keySet();
        assertFalse(interfaces.isEmpty(), "empty result");
        assertEquals(1, interfaces.size(), "more than one interface");
        final OperationInterface commonInterface = interfaces.stream()
            .findFirst()
            .get();
        assertEquals(expectedInterface, commonInterface, "common interface is not correct");

        final SortedSet<Operation> operations = simplifiedProvisions.get(commonInterface);
        assertEquals(2, operations.size(), "wrong number of operations in the interface");

        final List<Operation> firstMethodCandidates = operations.stream()
            .filter(x -> Optional.of("firstMethod")
                .equals(x.getName()
                    .forInterface("CommonInterface")))
            .collect(Collectors.toList());

        assertFalse(firstMethodCandidates.isEmpty(), "interface does not contain first method");
        assertEquals(1, firstMethodCandidates.size(), "interface contains multiple instances of first method");

        final List<Operation> secondMethodCandidates = operations.stream()
            .filter(x -> Optional.of("secondMethod")
                .equals(x.getName()
                    .forInterface("CommonInterface")))
            .collect(Collectors.toList());

        assertFalse(secondMethodCandidates.isEmpty(), "interface does not contain second method");
        assertEquals(1, secondMethodCandidates.size(), "interface contains multiple instances of second method");

        assertEquals(2, operations.size(), "interface contains additional operations");
    }

    @Test
    void entirePathInterface() {
        final ComponentBuilder builder = new ComponentBuilder(null);
        final Operation firstMethod = new Operation(null,
                new RESTName("test-host", "/common_interface/first_method", HTTPMethod.GET));
        final Operation secondMethod = new Operation(null,
                new RESTName("test-host", "/common_interface/second_method", HTTPMethod.GET));
        builder.provisions()
            .add(firstMethod);
        builder.provisions()
            .add(secondMethod);

        final List<OperationInterface> allDependencies = List.of(firstMethod, secondMethod);
        final List<OperationInterface> visibleProvisions = List.of(firstMethod, secondMethod);

        final Component builtComponent = builder.create(allDependencies, visibleProvisions);
        final EntireInterface expectedInterface = new EntireInterface(
                new RESTName("test-host", "/common_interface", HTTPMethod.GET));
        assertTrue(builtComponent.provisions()
            .containsPartOf(expectedInterface));

        final Map<OperationInterface, SortedSet<Operation>> simplifiedProvisions = builtComponent.provisions()
            .simplified();

        final Set<OperationInterface> interfaces = simplifiedProvisions.keySet();
        assertFalse(interfaces.isEmpty(), "empty result");
        assertEquals(1, interfaces.size(), "more than one interface");
        final OperationInterface commonInterface = interfaces.stream()
            .findFirst()
            .get();
        assertEquals(expectedInterface, commonInterface, "common interface is not correct");

        final SortedSet<Operation> operations = simplifiedProvisions.get(commonInterface);
        assertEquals(2, operations.size(), "wrong number of operations in the interface");

        final List<Operation> firstMethodCandidates = operations.stream()
            .filter(x -> Optional.of("test-host/common_interface/first_method[GET]")
                .equals(x.getName()
                    .forInterface("test-host/common_interface")))
            .collect(Collectors.toList());

        assertFalse(firstMethodCandidates.isEmpty(), "interface does not contain first method");
        assertEquals(1, firstMethodCandidates.size(), "interface contains multiple instances of first method");

        final List<Operation> secondMethodCandidates = operations.stream()
            .filter(x -> Optional.of("test-host/common_interface/second_method[GET]")
                .equals(x.getName()
                    .forInterface("test-host/common_interface")))
            .collect(Collectors.toList());

        assertFalse(secondMethodCandidates.isEmpty(), "interface does not contain second method");
        assertEquals(1, secondMethodCandidates.size(), "interface contains multiple instances of second method");

        assertEquals(2, operations.size(), "interface contains additional operations");
    }
}
