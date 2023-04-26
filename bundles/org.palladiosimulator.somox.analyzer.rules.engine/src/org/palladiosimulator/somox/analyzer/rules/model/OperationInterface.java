package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.List;
import java.util.Map;

public interface OperationInterface extends Comparable<OperationInterface> {
    Name getName();

    Map<String, List<Operation>> simplified();

    /**
     * @returns the most specific interface name
     */
    String getInterface();

    default boolean isPartOf(OperationInterface other) {
        return getName().isPartOf(other.getInterface());
    }

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
