package org.palladiosimulator.somox.analyzer.rules.test.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.palladiosimulator.somox.analyzer.rules.model.EntireInterface;
import org.palladiosimulator.somox.analyzer.rules.model.Operation;
import org.palladiosimulator.somox.analyzer.rules.model.PathName;

public class PathTest {

    @Test
    void pathNamesAreReflective() {
        String path = "/some/path";
        PathName pathName = new PathName(path);
        assertTrue(pathName.isPartOf(path));
    }

    @Test
    void pathsArePartOfTheirPrefixes() {
        String path = "/some/path";
        PathName interfaceName = new PathName(path);
        PathName specificName = new PathName(path + "/that/is/more/specific");

        assertTrue(specificName.isPartOf(interfaceName.getName()), "specific path is not part of its prefix");
        assertFalse(interfaceName.isPartOf(specificName.getName()), "prefix is part of a longer path");
    }

    @Test
    void prefixesAreSeparatorAware() {
        // This is NOT a legal prefix of "/some/path/..."
        String somePath = "/some/pa";
        EntireInterface entireInterface = new EntireInterface(new PathName(somePath));
        PathName specificPathName = new PathName("/some/path/that/is/more/specific");
        Operation operation = new Operation(null, specificPathName);

        assertFalse(operation.isPartOf(entireInterface), "operation is part of illegal prefix");
    }
}
