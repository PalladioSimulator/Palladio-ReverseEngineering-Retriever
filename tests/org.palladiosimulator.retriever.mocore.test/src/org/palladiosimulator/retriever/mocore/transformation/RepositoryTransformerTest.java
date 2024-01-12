package org.palladiosimulator.retriever.mocore.transformation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.palladiosimulator.retriever.mocore.utility.PcmEvaluationUtility.containsRepresentative;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.retriever.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.retriever.mocore.surrogate.element.AtomicComponent;
import org.palladiosimulator.retriever.mocore.surrogate.element.Component;
import org.palladiosimulator.retriever.mocore.surrogate.element.Composite;
import org.palladiosimulator.retriever.mocore.surrogate.element.Interface;
import org.palladiosimulator.retriever.mocore.surrogate.element.ServiceEffectSpecification;
import org.palladiosimulator.retriever.mocore.surrogate.element.Signature;
import org.palladiosimulator.retriever.mocore.surrogate.relation.ComponentSignatureProvisionRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.CompositionRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.InterfaceProvisionRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.InterfaceRequirementRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.ServiceEffectSpecificationRelation;
import org.palladiosimulator.retriever.mocore.surrogate.relation.SignatureProvisionRelation;

import tools.mdsd.mocore.framework.transformation.TransformerTest;

public class RepositoryTransformerTest extends TransformerTest<RepositoryTransformer, PcmSurrogate, Repository> {
    @Test
    public void testTransformSingleComponent() {
        // Test data
        RepositoryTransformer transformer = createTransformer();
        PcmSurrogate model = createEmptyModel();
        AtomicComponent component = AtomicComponent.getUniquePlaceholder();

        model.add(component);

        // Execution
        Repository repository = transformer.transform(model);

        // Assertion
        assertNotNull(repository);
        assertTrue(containsRepresentative(repository, component));
    }

    @Test
    public void testTransformSingleEmptyComposite() {
        // Test data
        RepositoryTransformer transformer = createTransformer();
        PcmSurrogate model = createEmptyModel();
        Composite composite = Composite.getUniquePlaceholder();

        model.add(composite);

        // Execution
        Repository repository = transformer.transform(model);

        // Assertion
        assertNotNull(repository);
        assertTrue(containsRepresentative(repository, composite));
    }

    @Test
    public void testTransformCompositeWithAtomicComponentChild() {
        // Test data
        RepositoryTransformer transformer = createTransformer();
        PcmSurrogate model = createEmptyModel();
        Composite composite = Composite.getUniquePlaceholder();
        AtomicComponent component = AtomicComponent.getUniquePlaceholder();
        CompositionRelation compositionRelation = new CompositionRelation(composite, component, false);

        model.add(composite);
        model.add(component);
        model.add(compositionRelation);

        // Execution
        Repository repository = transformer.transform(model);

        // Assertion
        assertNotNull(repository);
        assertTrue(containsRepresentative(repository, composite));
        assertTrue(containsRepresentative(repository, component));
        assertTrue(containsRepresentative(repository, compositionRelation));
    }

    @Test
    public void testTransformCompositeWithCompositeChild() {
        // Test data
        RepositoryTransformer transformer = createTransformer();
        PcmSurrogate model = createEmptyModel();
        Composite composite = Composite.getUniquePlaceholder();
        Composite child = Composite.getUniquePlaceholder();
        CompositionRelation compositionRelation = new CompositionRelation(composite, child, false);

        model.add(composite);
        model.add(child);
        model.add(compositionRelation);

        // Execution
        Repository repository = transformer.transform(model);

        // Assertion
        assertNotNull(repository);
        assertTrue(containsRepresentative(repository, composite));
        assertTrue(containsRepresentative(repository, child));
        assertTrue(containsRepresentative(repository, compositionRelation));
    }

    @Test
    public void testTransformCompositeWithMultilevelChildren() {
        // Test data
        RepositoryTransformer transformer = createTransformer();
        PcmSurrogate model = createEmptyModel();
        Composite compositeLevelZero = Composite.getUniquePlaceholder();
        Composite compositeLevelOne = Composite.getUniquePlaceholder();
        AtomicComponent component = AtomicComponent.getUniquePlaceholder();
        CompositionRelation compositionRelationFst = new CompositionRelation(compositeLevelZero, compositeLevelOne,
                false);
        CompositionRelation compositionRelationSnd = new CompositionRelation(compositeLevelOne, component, false);

        model.add(compositeLevelZero);
        model.add(compositeLevelOne);
        model.add(component);
        model.add(compositionRelationFst);
        model.add(compositionRelationSnd);

        // Execution
        Repository repository = transformer.transform(model);

        // Assertion
        assertNotNull(repository);
        assertTrue(containsRepresentative(repository, compositeLevelZero));
        assertTrue(containsRepresentative(repository, compositeLevelOne));
        assertTrue(containsRepresentative(repository, component));
        assertTrue(containsRepresentative(repository, compositionRelationFst));
        assertTrue(containsRepresentative(repository, compositionRelationSnd));
    }

    @Test
    public void testTransformSingleInterface() {
        // Test data
        RepositoryTransformer transformer = createTransformer();
        PcmSurrogate model = createEmptyModel();
        Interface element = Interface.getUniquePlaceholder();

        model.add(element);

        // Execution
        Repository repository = transformer.transform(model);

        // Assertion
        assertNotNull(repository);
        assertTrue(containsRepresentative(repository, element));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void testTransformInterfaceProvision(boolean isPlaceholderRelation) {
        // Test data
        RepositoryTransformer transformer = createTransformer();
        PcmSurrogate model = createEmptyModel();
        Component<?> provider = Component.getUniquePlaceholder();
        Interface providerInterface = Interface.getUniquePlaceholder();
        InterfaceProvisionRelation interfaceProvision = new InterfaceProvisionRelation(provider, providerInterface,
                isPlaceholderRelation);

        model.add(provider);
        model.add(providerInterface);
        model.add(interfaceProvision);

        // Execution
        Repository repository = transformer.transform(model);

        // Assertion
        assertNotNull(repository);
        assertTrue(containsRepresentative(repository, provider));
        assertTrue(containsRepresentative(repository, providerInterface));
        assertTrue(containsRepresentative(repository, interfaceProvision));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void testTransformInterfaceRequirement(boolean isPlaceholderRelation) {
        // Test data
        RepositoryTransformer transformer = createTransformer();
        PcmSurrogate model = createEmptyModel();
        Component<?> consumer = Component.getUniquePlaceholder();
        Interface consumerInterface = Interface.getUniquePlaceholder();
        InterfaceRequirementRelation interfaceRequirement = new InterfaceRequirementRelation(consumer,
                consumerInterface, isPlaceholderRelation);

        model.add(consumer);
        model.add(consumerInterface);
        model.add(interfaceRequirement);

        // Execution
        Repository repository = transformer.transform(model);

        // Assertion
        assertNotNull(repository);
        assertTrue(containsRepresentative(repository, consumer));
        assertTrue(containsRepresentative(repository, consumerInterface));
        assertTrue(containsRepresentative(repository, interfaceRequirement));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void testTransformSignatureProvision(boolean isPlaceholderRelation) {
        // Test data
        RepositoryTransformer transformer = createTransformer();
        PcmSurrogate model = createEmptyModel();
        Component<?> provider = Component.getUniquePlaceholder();
        Interface providerInterface = Interface.getUniquePlaceholder();
        InterfaceProvisionRelation interfaceProvision = new InterfaceProvisionRelation(provider, providerInterface,
                false);
        Signature signature = Signature.getUniquePlaceholder();
        SignatureProvisionRelation signatureProvision = new SignatureProvisionRelation(signature, providerInterface,
                isPlaceholderRelation);

        model.add(provider);
        model.add(providerInterface);
        model.add(interfaceProvision);
        model.add(signature);
        model.add(signatureProvision);

        // Execution
        Repository repository = transformer.transform(model);

        // Assertion
        assertNotNull(repository);
        assertTrue(containsRepresentative(repository, provider));
        assertTrue(containsRepresentative(repository, providerInterface));
        assertTrue(containsRepresentative(repository, interfaceProvision));
        assertTrue(containsRepresentative(repository, signatureProvision));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void testTransformComponentSignatureProvisionWithSeff(boolean isPlaceholderRelation) {
        // Test data
        RepositoryTransformer transformer = createTransformer();
        PcmSurrogate model = createEmptyModel();
        Component<?> provider = Component.getUniquePlaceholder();
        Interface providerInterface = Interface.getUniquePlaceholder();
        InterfaceProvisionRelation interfaceProvision = new InterfaceProvisionRelation(provider, providerInterface,
                false);
        Signature signature = Signature.getUniquePlaceholder();
        SignatureProvisionRelation signatureProvision = new SignatureProvisionRelation(signature, providerInterface,
                false);
        ComponentSignatureProvisionRelation componentSignatureProvisionRelation = new ComponentSignatureProvisionRelation(
                interfaceProvision, signatureProvision, false);
        ServiceEffectSpecification seff = ServiceEffectSpecification.getUniquePlaceholder();
        ServiceEffectSpecificationRelation seffRelation = new ServiceEffectSpecificationRelation(
                componentSignatureProvisionRelation, seff, isPlaceholderRelation);

        model.add(provider);
        model.add(providerInterface);
        model.add(interfaceProvision);
        model.add(signature);
        model.add(signatureProvision);
        model.add(componentSignatureProvisionRelation);
        model.add(seff);
        model.add(seffRelation);

        // Execution
        Repository repository = transformer.transform(model);

        // Assertion
        assertNotNull(repository);
        assertTrue(containsRepresentative(repository, provider));
        assertTrue(containsRepresentative(repository, providerInterface));
        assertTrue(containsRepresentative(repository, interfaceProvision));
        assertTrue(containsRepresentative(repository, signatureProvision));
        assertTrue(containsRepresentative(repository, seffRelation));
    }

    @Override
    protected RepositoryTransformer createTransformer() {
        return new RepositoryTransformer();
    }

    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }
}
