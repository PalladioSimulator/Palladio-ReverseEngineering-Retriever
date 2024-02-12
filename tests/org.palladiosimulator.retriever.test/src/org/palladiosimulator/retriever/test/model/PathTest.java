package org.palladiosimulator.retriever.test.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        final RESTName pathName = new RESTName(host, path, HTTPMethod.any());
        assertTrue(pathName.isPartOf(host + path));
    }

    @Test
    void pathsArePartOfTheirPrefixes() {
        final String path = "/some/path";
        final RESTName interfaceName = new RESTName("test-host", path, HTTPMethod.any());
        final RESTName specificName = new RESTName("test-host", path + "/that/is/more/specific", HTTPMethod.GET);

        assertTrue(specificName.isPartOf(interfaceName.getName()), "specific path is not part of its prefix");
        assertFalse(interfaceName.isPartOf(specificName.getName()), "prefix is part of a longer path");
    }

    @Test
    void prefixesAreSeparatorAware() {
        // This is NOT a legal prefix of "/some/path/..."
        final String somePath = "/some/pa";
        final EntireInterface entireInterface = new EntireInterface(
                new RESTName("test-host", somePath, HTTPMethod.any()));
        final RESTName specificPathName = new RESTName("test-host", "/some/path/that/is/more/specific",
                HTTPMethod.all());
        final Operation operation = new Operation(null, specificPathName);

        assertFalse(operation.isPartOf(entireInterface), "operation is part of illegal prefix");
    }

    @Test
    void httpMethodsAreSpecializations() {
        final String path = "/some/path";
        final RESTName generalRequirementName = new RESTName("test-host", path, HTTPMethod.any());
        final RESTName specificName = new RESTName("test-host", path, HTTPMethod.GET);
        final RESTName generalProvisionName = new RESTName("test-host", path, HTTPMethod.all());

        final Operation generalRequirementOperation = new Operation(null, generalRequirementName);
        final Operation specificOperation = new Operation(null, specificName);
        final Operation generalProvisionOperation = new Operation(null, generalProvisionName);

        assertTrue(specificOperation.isPartOf(generalRequirementOperation));
        assertFalse(generalProvisionOperation.isPartOf(specificOperation));
    }
}
