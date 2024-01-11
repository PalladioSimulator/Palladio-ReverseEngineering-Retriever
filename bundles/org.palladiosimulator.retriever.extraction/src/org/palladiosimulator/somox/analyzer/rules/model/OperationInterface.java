package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.List;
import java.util.Map;

public interface OperationInterface extends Comparable<OperationInterface> {
    Name getName();

    Map<OperationInterface, List<Operation>> simplified();

    /**
     * @returns the most specific interface name (and the interface name for java)
     */
    String getInterface();

    default boolean isPartOf(OperationInterface other) {
        return getName().isPartOf(other.getName()
            .toString());
    }

    @Override
    default int compareTo(OperationInterface other) {
        boolean isSubset = this.isPartOf(other);
        boolean isSuperset = other.isPartOf(this);
        if (isSubset && isSuperset) {
            return 0; // equal
        } else if (isSubset) {
            return 1;
        } else if (isSuperset) {
            return -1;
        }

        return getName().toString()
            .compareTo(other.getName()
                .toString()); // disjoint
    }
}
