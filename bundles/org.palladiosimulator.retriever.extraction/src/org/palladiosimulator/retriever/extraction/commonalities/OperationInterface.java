package org.palladiosimulator.retriever.extraction.commonalities;

import java.util.List;
import java.util.Map;

public interface OperationInterface extends Comparable<OperationInterface> {
    Name getName();

    Map<OperationInterface, List<Operation>> simplified();

    /**
     * @returns the most specific interface name (and the interface name for java)
     */
    String getInterface();

    default boolean isPartOf(final OperationInterface other) {
        return this.getName()
            .isPartOf(other.getName()
                .toString());
    }

    @Override
    default int compareTo(final OperationInterface other) {
        final boolean isSubset = this.isPartOf(other);
        final boolean isSuperset = other.isPartOf(this);
        if (isSubset && isSuperset) {
            return 0; // equal
        } else if (isSubset) {
            return 1;
        } else if (isSuperset) {
            return -1;
        }

        return this.getName()
            .toString()
            .compareTo(other.getName()
                .toString()); // disjoint
    }
}
