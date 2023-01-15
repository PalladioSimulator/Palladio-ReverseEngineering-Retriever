package org.palladiosimulator.somox.analyzer.rules.mocore.discovery;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.palladiosimulator.generator.fluent.repository.factory.FluentRepositoryFactory;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Component;

import tools.mdsd.mocore.framework.discovery.Discoverer;

@SuppressWarnings("unchecked")
public class RepositoryDecompositorTest extends DecompositorTest<RepositoryDecompositor, Repository> {
    @Test
    public void testDecomposeEmptyRepository() {
        RepositoryDecompositor decompositor = createDecompositor();
        Repository repository = createEmptyRepository();

        Collection<Discoverer<?>> discoverers = decompositor.decompose(repository);
        assertEquals(5, discoverers.size());
        discoverers.forEach((discoverer) -> assertTrue(discoverer.getDiscoveries().isEmpty()));
    }

    @Test
    public void testDecomposeUncoupledComponents() {
        RepositoryDecompositor decompositor = createDecompositor();
        FluentRepositoryFactory factory = new FluentRepositoryFactory();
        Repository repository = factory.newRepository()
                .addToRepository(factory.newBasicComponent().withName("Component_1"))
                .addToRepository(factory.newBasicComponent().withName("Component_2"))
                .addToRepository(factory.newBasicComponent().withName("Component_3"))
                .addToRepository(factory.newBasicComponent().withName("Component_4"))
                .addToRepository(factory.newBasicComponent().withName("Component_5"))
                .createRepositoryNow();

        Collection<Discoverer<?>> discoverers = decompositor.decompose(repository);
        assertFalse(discoverers.isEmpty());
        assertEquals(5, discoverers.size());

        // Remove all discoverers except component discoverer
        List<Discoverer<?>> modifiableDiscoverers = new ArrayList<>(discoverers);
        modifiableDiscoverers.removeIf((discoverer) -> discoverer.getDiscoveryType() != Component.class);
        assertEquals(1, modifiableDiscoverers.size());

        // Get and check component discoverer
        Discoverer<Component> componentDiscoverer = (Discoverer<Component>) modifiableDiscoverers.iterator().next();
        assertEquals(5, componentDiscoverer.getDiscoveries().size());
        for (int i = 1; i <= 5; i++) {
            final int j = i;
            assertTrue(componentDiscoverer.getDiscoveries().stream()
                    .anyMatch((Component component) -> component.getValue().getEntityName().equals("Component_" + j)));
        }
    }

    // TODO Add tests for interface, signatures, seff, and all relevant relations

    @Override
    protected RepositoryDecompositor createDecompositor() {
        return new RepositoryDecompositor();
    }

    @Override
    protected Repository createValidSource() {
        return createEmptyRepository();
    }

    private Repository createEmptyRepository() {
        return new FluentRepositoryFactory().newRepository().createRepositoryNow();
    }
}
