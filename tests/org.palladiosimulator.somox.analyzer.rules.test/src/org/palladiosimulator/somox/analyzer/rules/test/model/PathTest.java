package org.palladiosimulator.somox.analyzer.rules.test.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.palladiosimulator.somox.analyzer.rules.model.EntireInterface;
import org.palladiosimulator.somox.analyzer.rules.model.HTTPMethod;
import org.palladiosimulator.somox.analyzer.rules.model.Operation;
import org.palladiosimulator.somox.analyzer.rules.model.RESTName;

public class PathTest {

    @Test
    void pathNamesAreReflective() {
        String host = "test-host";
        String path = "/some/path";
        RESTName pathName = new RESTName(host, path, Optional.empty());
        assertTrue(pathName.isPartOf(host + path));
    }

    @Test
    void pathsArePartOfTheirPrefixes() {
        String path = "/some/path";
        RESTName interfaceName = new RESTName("test-host", path, Optional.empty());
        RESTName specificName = new RESTName("test-host", path + "/that/is/more/specific", Optional.empty());

        assertTrue(specificName.isPartOf(interfaceName.getName()), "specific path is not part of its prefix");
        assertFalse(interfaceName.isPartOf(specificName.getName()), "prefix is part of a longer path");
    }

    @Test
    void prefixesAreSeparatorAware() {
        // This is NOT a legal prefix of "/some/path/..."
        String somePath = "/some/pa";
        EntireInterface entireInterface = new EntireInterface(new RESTName("test-host", somePath, Optional.empty()));
        RESTName specificPathName = new RESTName("test-host", "/some/path/that/is/more/specific", Optional.empty());
        Operation operation = new Operation(null, specificPathName);

        assertFalse(operation.isPartOf(entireInterface), "operation is part of illegal prefix");
    }

    @Disabled("This requirement has been temporarily softened for the Spring Gateway")
    @Test
    void httpMethodsAreSpecializations() {
        String path = "/some/path";
        RESTName generalName = new RESTName("test-host", path, Optional.empty());
        RESTName specificName = new RESTName("test-host", path, Optional.of(HTTPMethod.GET));

        Operation generalOperation = new Operation(null, generalName);
        Operation specificOperation = new Operation(null, specificName);

        assertTrue(specificOperation.isPartOf(generalOperation));
        assertFalse(generalOperation.isPartOf(specificOperation));
    }
}
