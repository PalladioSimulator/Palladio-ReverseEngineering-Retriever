package org.palladiosimulator.retriever.test.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.palladiosimulator.retriever.extraction.commonalities.EntireInterface;
import org.palladiosimulator.retriever.extraction.commonalities.HTTPMethod;
import org.palladiosimulator.retriever.extraction.commonalities.Operation;
import org.palladiosimulator.retriever.extraction.commonalities.RESTName;
import org.palladiosimulator.retriever.extraction.commonalities.RESTOperationName;
import org.palladiosimulator.retriever.extraction.commonalities.RESTOperationUnion;

public class PathTest {

    @Test
    void pathNamesAreReflective() {
        final String host = "test-host";
        final String path = "/some/path";
        final RESTName pathName = new RESTName(host, path);
        assertTrue(pathName.isPartOf(host + path));
    }

    @Test
    void pathsArePartOfTheirPrefixes() {
        final String path = "/some/path";
        final RESTName interfaceName = new RESTName("test-host", path);
        final RESTName specificName = new RESTName("test-host", path + "/that/is/more/specific");

        assertTrue(specificName.isPartOf(interfaceName.getName()), "specific path is not part of its prefix");
        assertFalse(interfaceName.isPartOf(specificName.getName()), "prefix is part of a longer path");
    }

    @Test
    void prefixesAreSeparatorAware() {
        // This is NOT a legal prefix of "/some/path/..."
        final String somePath = "/some/pa";
        final EntireInterface entireInterface = new EntireInterface(new RESTName("test-host", somePath));
        final RESTOperationName specificPathName = new RESTOperationName("test-host",
                "/some/path/that/is/more/specific");
        final Operation operation = new Operation(null, specificPathName);

        assertFalse(operation.isPartOf(entireInterface), "operation is part of illegal prefix");
    }

    @Test
    void httpMethodsMatchCorrectly() {
        final String path = "/some/path";
        final RESTOperationName generalName = new RESTOperationName("test-host", path, HTTPMethod.WILDCARD);
        final RESTOperationName specificName = new RESTOperationName("test-host", path, HTTPMethod.GET);

        final RESTOperationUnion generalOperation = new RESTOperationUnion(generalName);
        final RESTOperationUnion specificOperation = new RESTOperationUnion(specificName);

        assertTrue(specificOperation.isPartOf(generalOperation));
        assertFalse(generalOperation.isPartOf(specificOperation));
    }
}
