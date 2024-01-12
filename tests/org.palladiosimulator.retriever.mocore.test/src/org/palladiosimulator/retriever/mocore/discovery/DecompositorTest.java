package org.palladiosimulator.retriever.mocore.discovery;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.jupiter.api.Test;

import tools.mdsd.mocore.framework.discovery.Discoverer;

public abstract class DecompositorTest<D extends Decompositor<T>, T> {
    @Test
    public void testDecomposeWithValidSource() {
        D decompositor = this.createDecompositor();
        T source = this.createValidSource();
        Collection<Discoverer<?>> discoverers = decompositor.decompose(source);
        assertNotNull(discoverers);
        assertTrue(discoverers.size() >= 0);
    }

    @Test
    public void testDecomposeWithNullSource() {
        D decompositor = this.createDecompositor();
        T source = null;
        assertThrows(NullPointerException.class, () -> decompositor.decompose(source));
    }

    protected abstract D createDecompositor();

    protected abstract T createValidSource();
}
