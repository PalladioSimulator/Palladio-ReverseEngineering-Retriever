package org.palladiosimulator.somox.analyzer.rules.test.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.palladiosimulator.somox.analyzer.rules.model.EntireInterface;
import org.palladiosimulator.somox.analyzer.rules.model.HTTPMethod;
import org.palladiosimulator.somox.analyzer.rules.model.Operation;
import org.palladiosimulator.somox.analyzer.rules.model.RESTName;

public class PathTest {

    @Test
    void pathNamesAreReflective() {
        String path = "/some/path";
        RESTName pathName = new RESTName(path, Optional.empty());
        assertTrue(pathName.isPartOf(path));
    }

    @Test
    void pathsArePartOfTheirPrefixes() {
        String path = "/some/path";
        RESTName interfaceName = new RESTName(path, Optional.empty());
        RESTName specificName = new RESTName(path + "/that/is/more/specific", Optional.empty());

        assertTrue(specificName.isPartOf(interfaceName.getName()), "specific path is not part of its prefix");
        assertFalse(interfaceName.isPartOf(specificName.getName()), "prefix is part of a longer path");
    }

    @Test
    void prefixesAreSeparatorAware() {
        // This is NOT a legal prefix of "/some/path/..."
        String somePath = "/some/pa";
        EntireInterface entireInterface = new EntireInterface(new RESTName(somePath, Optional.empty()));
        RESTName specificPathName = new RESTName("/some/path/that/is/more/specific", Optional.empty());
        Operation operation = new Operation(null, specificPathName);

        assertFalse(operation.isPartOf(entireInterface), "operation is part of illegal prefix");
    }

    @Test
    void httpMethodsAreSpecializations() {
        String path = "/some/path";
        RESTName generalName = new RESTName(path, Optional.empty());
        RESTName specificName = new RESTName(path, Optional.of(HTTPMethod.GET));

        Operation generalOperation = new Operation(null, generalName);
        Operation specificOperation = new Operation(null, specificName);

        assertTrue(specificOperation.isPartOf(generalOperation));
        assertFalse(generalOperation.isPartOf(specificOperation));
    }
}
