package org.palladiosimulator.retriever.extraction.engine;

import org.eclipse.jdt.core.dom.ITypeBinding;

public final class NameConverter {
    private NameConverter() {
        throw new IllegalStateException();
    }

    public static String toPCMIdentifier(final ITypeBinding name) {
        final String fullName = name.getQualifiedName()
            .replace(".", "_");
        // Erase type parameters in identifiers
        // TODO is this the right solution?
        if (fullName.contains("<")) {
            return fullName.substring(0, fullName.indexOf('<'));
        }
        return fullName;
    }
}
