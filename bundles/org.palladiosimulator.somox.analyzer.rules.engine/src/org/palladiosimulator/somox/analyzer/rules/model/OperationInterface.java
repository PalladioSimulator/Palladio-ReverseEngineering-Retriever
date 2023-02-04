package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.List;
import java.util.Map;

public interface OperationInterface extends Comparable<OperationInterface> {
    String getInterface();

    boolean isPartOf(OperationInterface other);

    boolean isEntireInterface();

    Map<String, List<Operation>> simplified();

    @Override
    default int compareTo(OperationInterface other) {
        boolean isSubset = isPartOf(other);
        boolean isSuperset = isPartOf(other);
        if (isSubset && isSuperset) {
            return 0; // equal
        } else if (isSubset) {
            return 1;
        } else if (isSuperset) {
            return -1;
        }

        return 0; // disjoint
    }
}
