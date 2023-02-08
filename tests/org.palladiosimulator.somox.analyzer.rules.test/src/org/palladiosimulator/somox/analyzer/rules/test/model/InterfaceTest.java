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
import org.palladiosimulator.somox.analyzer.rules.model.PathName;

public class InterfaceTest {

    @Test
    void singleJavaOperation() {
        ComponentBuilder builder = new ComponentBuilder(null);
        Operation expectedOperation = new Operation(null, new JavaOperationName("Interface", "method"));
        builder.provisions()
            .add(expectedOperation);

        Component builtComponent = builder.create();
        assertTrue(builtComponent.provisions()
            .contains(expectedOperation));

        Map<String, List<Operation>> simplifiedProvisions = builtComponent.provisions()
            .simplified();

        Set<String> interfaces = simplifiedProvisions.keySet();
        assertFalse(interfaces.isEmpty(), "empty result");
        assertEquals(1, interfaces.size(), "more than one interface");
        String commonInterface = interfaces.stream()
            .findFirst()
            .get();
        assertEquals("Interface", commonInterface, "operation does not have the correct interface");

        List<Operation> operations = simplifiedProvisions.get("Interface");
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
        Operation expectedOperation = new Operation(null, new PathName("/method"));
        builder.provisions()
            .add(expectedOperation);

        Component builtComponent = builder.create();
        assertTrue(builtComponent.provisions()
            .contains(expectedOperation));

        Map<String, List<Operation>> simplifiedProvisions = builtComponent.provisions()
            .simplified();

        Set<String> interfaces = simplifiedProvisions.keySet();
        assertFalse(interfaces.isEmpty(), "empty result");
        assertEquals(1, interfaces.size(), "more than one interface");
        String commonInterface = interfaces.stream()
            .findFirst()
            .get();
        assertEquals("/method", commonInterface, "operation does not have the correct interface");

        List<Operation> operations = simplifiedProvisions.get("/method");
        assertEquals(1, operations.size(), "more than one operation in the interface");

        List<Operation> firstMethodCandidates = operations.stream()
            .filter(x -> Optional.of("")
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
        builder.provisions()
            .add(new Operation(null, new JavaOperationName("CommonInterface", "firstMethod")));
        builder.provisions()
            .add(new Operation(null, new JavaOperationName("CommonInterface", "secondMethod")));
        Component builtComponent = builder.create();
        EntireInterface expectedInterface = new EntireInterface(new JavaInterfaceName("CommonInterface"));
        assertTrue(builtComponent.provisions()
            .contains(expectedInterface));

        Map<String, List<Operation>> simplifiedProvisions = builtComponent.provisions()
            .simplified();

        Set<String> interfaces = simplifiedProvisions.keySet();
        assertFalse(interfaces.isEmpty(), "empty result");
        assertEquals(1, interfaces.size(), "more than one interface");
        String commonInterface = interfaces.stream()
            .findFirst()
            .get();
        assertEquals("CommonInterface", commonInterface, "common interface does not have the correct name");

        List<Operation> operations = simplifiedProvisions.get("CommonInterface");
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
        builder.provisions()
            .add(new Operation(null, new PathName("/common_interface/first_method")));
        builder.provisions()
            .add(new Operation(null, new PathName("/common_interface/second_method")));
        Component builtComponent = builder.create();
        EntireInterface expectedInterface = new EntireInterface(new PathName("/common_interface"));
        assertTrue(builtComponent.provisions()
            .contains(expectedInterface));

        Map<String, List<Operation>> simplifiedProvisions = builtComponent.provisions()
            .simplified();

        Set<String> interfaces = simplifiedProvisions.keySet();
        assertFalse(interfaces.isEmpty(), "empty result");
        assertEquals(1, interfaces.size(), "more than one interface");
        String commonInterface = interfaces.stream()
            .findFirst()
            .get();
        assertEquals("/common_interface", commonInterface, "common interface does not have the correct name");

        List<Operation> operations = simplifiedProvisions.get("/common_interface");
        assertEquals(2, operations.size(), "wrong number of operations in the interface");

        List<Operation> firstMethodCandidates = operations.stream()
            .filter(x -> Optional.of("first_method")
                .equals(x.getName()
                    .forInterface("/common_interface")))
            .collect(Collectors.toList());

        assertFalse(firstMethodCandidates.isEmpty(), "interface does not contain first method");
        assertEquals(1, firstMethodCandidates.size(), "interface contains multiple instances of first method");

        List<Operation> secondMethodCandidates = operations.stream()
            .filter(x -> Optional.of("second_method")
                .equals(x.getName()
                    .forInterface("/common_interface")))
            .collect(Collectors.toList());

        assertFalse(secondMethodCandidates.isEmpty(), "interface does not contain second method");
        assertEquals(1, secondMethodCandidates.size(), "interface contains multiple instances of second method");

        assertEquals(2, operations.size(), "interface contains additional operations");
    }
}
