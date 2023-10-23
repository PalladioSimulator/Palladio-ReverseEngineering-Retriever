package org.palladiosimulator.somox.analyzer.rules.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.palladiosimulator.somox.analyzer.rules.model.Component;
import org.palladiosimulator.somox.analyzer.rules.model.ComponentBuilder;
import org.palladiosimulator.somox.analyzer.rules.model.EntireInterface;
import org.palladiosimulator.somox.analyzer.rules.model.JavaInterfaceName;
import org.palladiosimulator.somox.analyzer.rules.model.JavaOperationName;
import org.palladiosimulator.somox.analyzer.rules.model.Operation;
import org.palladiosimulator.somox.analyzer.rules.model.OperationInterface;
import org.palladiosimulator.somox.analyzer.rules.model.RESTName;

public class InterfaceTest {

    @Test
    void singleJavaOperation() {
        ComponentBuilder builder = new ComponentBuilder(null);
        Operation expectedOperation = new Operation(null, new JavaOperationName("Interface", "method"));
        builder.provisions()
            .add(expectedOperation);

        Component builtComponent = builder.create(List.of(expectedOperation), List.of(expectedOperation));
        assertTrue(builtComponent.provisions()
            .containsPartOf(expectedOperation));

        Map<OperationInterface, List<Operation>> simplifiedProvisions = builtComponent.provisions()
            .simplified();

        Set<OperationInterface> interfaces = simplifiedProvisions.keySet();
        assertFalse(interfaces.isEmpty(), "empty result");
        assertEquals(1, interfaces.size(), "more than one interface");
        OperationInterface commonInterface = interfaces.stream()
            .findFirst()
            .get();
        assertEquals(expectedOperation, commonInterface, "operation does not have the correct interface");

        List<Operation> operations = simplifiedProvisions.get(expectedOperation);
        assertEquals(1, operations.size(), "more than one operation in the interface");

        List<Operation> firstMethodCandidates = operations.stream()
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
        ComponentBuilder builder = new ComponentBuilder(null);
        Operation expectedOperation = new Operation(null, new RESTName("/method", Optional.empty()));
        builder.provisions()
            .add(expectedOperation);

        Component builtComponent = builder.create(List.of(expectedOperation), List.of(expectedOperation));
        assertTrue(builtComponent.provisions()
            .containsPartOf(expectedOperation));

        Map<OperationInterface, List<Operation>> simplifiedProvisions = builtComponent.provisions()
            .simplified();

        Set<OperationInterface> interfaces = simplifiedProvisions.keySet();
        assertFalse(interfaces.isEmpty(), "empty result");
        assertEquals(1, interfaces.size(), "more than one interface");
        OperationInterface commonInterface = interfaces.stream()
            .findFirst()
            .get();
        assertEquals(expectedOperation, commonInterface, "operation does not have the correct interface");

        List<Operation> operations = simplifiedProvisions.get(commonInterface);
        assertEquals(1, operations.size(), "more than one operation in the interface");

        List<Operation> firstMethodCandidates = operations.stream()
            .filter(x -> Optional.of("/method")
                .equals(x.getName()
                    .forInterface("/method")))
            .collect(Collectors.toList());

        assertFalse(firstMethodCandidates.isEmpty(), "interface does not contain an operation");
        assertEquals(1, firstMethodCandidates.size(), "interface contains multiple instances of the operation");
        assertEquals(1, operations.size(), "interface contains additional operations");
    }

    @Test
    void entireJavaInterface() {
        ComponentBuilder builder = new ComponentBuilder(null);
        Operation firstMethod = new Operation(null, new JavaOperationName("CommonInterface", "firstMethod"));
        Operation secondMethod = new Operation(null, new JavaOperationName("CommonInterface", "secondMethod"));
        builder.provisions()
            .add(firstMethod);
        builder.provisions()
            .add(secondMethod);
        Component builtComponent = builder.create(List.of(firstMethod, secondMethod),
                List.of(firstMethod, secondMethod));
        EntireInterface expectedInterface = new EntireInterface(new JavaInterfaceName("CommonInterface"));
        assertTrue(builtComponent.provisions()
            .containsPartOf(expectedInterface));

        Map<OperationInterface, List<Operation>> simplifiedProvisions = builtComponent.provisions()
            .simplified();

        Set<OperationInterface> interfaces = simplifiedProvisions.keySet();
        assertFalse(interfaces.isEmpty(), "empty result");
        assertEquals(1, interfaces.size(), "more than one interface");
        OperationInterface commonInterface = interfaces.stream()
            .findFirst()
            .get();
        assertEquals(expectedInterface, commonInterface, "common interface is not correct");

        List<Operation> operations = simplifiedProvisions.get(commonInterface);
        assertEquals(2, operations.size(), "wrong number of operations in the interface");

        List<Operation> firstMethodCandidates = operations.stream()
            .filter(x -> Optional.of("firstMethod")
                .equals(x.getName()
                    .forInterface("CommonInterface")))
            .collect(Collectors.toList());

        assertFalse(firstMethodCandidates.isEmpty(), "interface does not contain first method");
        assertEquals(1, firstMethodCandidates.size(), "interface contains multiple instances of first method");

        List<Operation> secondMethodCandidates = operations.stream()
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
        ComponentBuilder builder = new ComponentBuilder(null);
        Operation firstMethod = new Operation(null, new RESTName("/common_interface/first_method", Optional.empty()));
        Operation secondMethod = new Operation(null, new RESTName("/common_interface/second_method", Optional.empty()));
        builder.provisions()
            .add(firstMethod);
        builder.provisions()
            .add(secondMethod);
        Component builtComponent = builder.create(List.of(firstMethod, secondMethod),
                List.of(firstMethod, secondMethod));
        EntireInterface expectedInterface = new EntireInterface(new RESTName("/common_interface", Optional.empty()));
        assertTrue(builtComponent.provisions()
            .containsPartOf(expectedInterface));

        Map<OperationInterface, List<Operation>> simplifiedProvisions = builtComponent.provisions()
            .simplified();

        Set<OperationInterface> interfaces = simplifiedProvisions.keySet();
        assertFalse(interfaces.isEmpty(), "empty result");
        assertEquals(1, interfaces.size(), "more than one interface");
        OperationInterface commonInterface = interfaces.stream()
            .findFirst()
            .get();
        assertEquals(expectedInterface, commonInterface, "common interface is not correct");

        List<Operation> operations = simplifiedProvisions.get(commonInterface);
        assertEquals(2, operations.size(), "wrong number of operations in the interface");

        List<Operation> firstMethodCandidates = operations.stream()
            .filter(x -> Optional.of("/common_interface/first_method")
                .equals(x.getName()
                    .forInterface("/common_interface")))
            .collect(Collectors.toList());

        assertFalse(firstMethodCandidates.isEmpty(), "interface does not contain first method");
        assertEquals(1, firstMethodCandidates.size(), "interface contains multiple instances of first method");

        List<Operation> secondMethodCandidates = operations.stream()
            .filter(x -> Optional.of("/common_interface/second_method")
                .equals(x.getName()
                    .forInterface("/common_interface")))
            .collect(Collectors.toList());

        assertFalse(secondMethodCandidates.isEmpty(), "interface does not contain second method");
        assertEquals(1, secondMethodCandidates.size(), "interface contains multiple instances of second method");

        assertEquals(2, operations.size(), "interface contains additional operations");
    }
}
