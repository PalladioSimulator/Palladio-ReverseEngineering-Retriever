package org.palladiosimulator.retriever.test.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.palladiosimulator.retriever.extraction.commonalities.EntireInterface;
import org.palladiosimulator.retriever.extraction.commonalities.HTTPMethod;
import org.palladiosimulator.retriever.extraction.commonalities.Operation;
import org.palladiosimulator.retriever.extraction.commonalities.RESTName;

public class PathTest {

    @Test
    void pathNamesAreReflective() {
        final String host = "test-host";
        final String path = "/some/path";
        final RESTName pathName = new RESTName(host, path, Optional.empty());
        assertTrue(pathName.isPartOf(host + path));
    }

    @Test
    void pathsArePartOfTheirPrefixes() {
        final String path = "/some/path";
        final RESTName interfaceName = new RESTName("test-host", path, Optional.empty());
        final RESTName specificName = new RESTName("test-host", path + "/that/is/more/specific", Optional.empty());

        assertTrue(specificName.isPartOf(interfaceName.getName()), "specific path is not part of its prefix");
        assertFalse(interfaceName.isPartOf(specificName.getName()), "prefix is part of a longer path");
    }

    @Test
    void prefixesAreSeparatorAware() {
        // This is NOT a legal prefix of "/some/path/..."
        final String somePath = "/some/pa";
        final EntireInterface entireInterface = new EntireInterface(
                new RESTName("test-host", somePath, Optional.empty()));
        final RESTName specificPathName = new RESTName("test-host", "/some/path/that/is/more/specific",
                Optional.empty());
        final Operation operation = new Operation(null, specificPathName);

        assertFalse(operation.isPartOf(entireInterface), "operation is part of illegal prefix");
    }

    @Disabled("This requirement has been temporarily softened for the Spring Gateway")
    @Test
    void httpMethodsAreSpecializations() {
        final String path = "/some/path";
        final RESTName generalName = new RESTName("test-host", path, Optional.empty());
        final RESTName specificName = new RESTName("test-host", path, Optional.of(HTTPMethod.GET));

        final Operation generalOperation = new Operation(null, generalName);
        final Operation specificOperation = new Operation(null, specificName);

        assertTrue(specificOperation.isPartOf(generalOperation));
        assertFalse(generalOperation.isPartOf(specificOperation));
    }
}
