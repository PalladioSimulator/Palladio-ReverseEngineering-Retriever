package org.palladiosimulator.retriever.mocore.discovery;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.palladiosimulator.generator.fluent.repository.factory.FluentRepositoryFactory;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.retriever.mocore.surrogate.element.AtomicComponent;

import tools.mdsd.mocore.framework.discovery.Discoverer;

@SuppressWarnings("unchecked")
public class RepositoryDecompositorTest extends DecompositorTest<RepositoryDecompositor, Repository> {
    @Test
    public void testDecomposeEmptyRepository() {
        final RepositoryDecompositor decompositor = this.createDecompositor();
        final Repository repository = this.createEmptyRepository();

        final Collection<Discoverer<?>> discoverers = decompositor.decompose(repository);
        assertEquals(10, discoverers.size());
        discoverers.forEach((discoverer) -> assertTrue(discoverer.getDiscoveries()
            .isEmpty()));
    }

    @Test
    public void testDecomposeUncoupledComponents() {
        final RepositoryDecompositor decompositor = this.createDecompositor();
        final FluentRepositoryFactory factory = new FluentRepositoryFactory();
        final Repository repository = factory.newRepository()
            .addToRepository(factory.newBasicComponent()
                .withName("Component_1"))
            .addToRepository(factory.newBasicComponent()
                .withName("Component_2"))
            .addToRepository(factory.newBasicComponent()
                .withName("Component_3"))
            .addToRepository(factory.newBasicComponent()
                .withName("Component_4"))
            .addToRepository(factory.newBasicComponent()
                .withName("Component_5"))
            .createRepositoryNow();

        final Collection<Discoverer<?>> discoverers = decompositor.decompose(repository);
        assertFalse(discoverers.isEmpty());

        // Remove all discoverers except component discoverer
        final List<Discoverer<?>> modifiableDiscoverers = new ArrayList<>(discoverers);
        modifiableDiscoverers.removeIf((discoverer) -> discoverer.getDiscoveryType() != AtomicComponent.class);
        assertEquals(1, modifiableDiscoverers.size());

        // Get and check component discoverer
        final Discoverer<AtomicComponent> componentDiscoverer = (Discoverer<AtomicComponent>) modifiableDiscoverers
            .iterator()
            .next();
        assertEquals(5, componentDiscoverer.getDiscoveries()
            .size());
        for (int i = 1; i <= 5; i++) {
            final int j = i;
            assertTrue(componentDiscoverer.getDiscoveries()
                .stream()
                .anyMatch((final AtomicComponent component) -> component.getValue()
                    .getEntityName()
                    .equals("Component_" + j)));
        }
    }

    // TODO Add tests for interface, signatures, seff, and all relevant relations
    // TODO Add tests for composition, delegation

    @Override
    protected RepositoryDecompositor createDecompositor() {
        return new RepositoryDecompositor();
    }

    @Override
    protected Repository createValidSource() {
        return this.createEmptyRepository();
    }

    private Repository createEmptyRepository() {
        return new FluentRepositoryFactory().newRepository()
            .createRepositoryNow();
    }
}
