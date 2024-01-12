package org.palladiosimulator.retriever.core.main;

/**
 * A general exception for Retriever.
 *
 * @author Florian Bossert
 */
public class RetrieverException extends Exception {

    private static final long serialVersionUID = 8438995877350048404L;

    public RetrieverException(final String message) {
        super(message);
    }

    public RetrieverException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
