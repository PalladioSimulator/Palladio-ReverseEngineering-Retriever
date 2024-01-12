package org.palladiosimulator.retriever.extraction.commonalities;

import java.util.Optional;

public interface OperationName extends Name {
    Optional<String> forInterface(String baseInterface);

    // Returns the most specific entire interface.
    String getInterface();
}