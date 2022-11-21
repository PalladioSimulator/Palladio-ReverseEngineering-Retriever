package com.gstuer.modelmerging.instance.pcm.transformation;

import static com.gstuer.modelmerging.instance.pcm.utility.PcmEvaluationUtility.containsRepresentative;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.palladiosimulator.pcm.repository.Repository;

import com.gstuer.modelmerging.framework.transformation.TransformerTest;
import com.gstuer.modelmerging.instance.pcm.surrogate.PcmSurrogate;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Component;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Interface;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.ServiceEffectSpecification;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Signature;
import com.gstuer.modelmerging.instance.pcm.surrogate.relation.ComponentSignatureProvisionRelation;
import com.gstuer.modelmerging.instance.pcm.surrogate.relation.InterfaceProvisionRelation;
import com.gstuer.modelmerging.instance.pcm.surrogate.relation.InterfaceRequirementRelation;
import com.gstuer.modelmerging.instance.pcm.surrogate.relation.ServiceEffectSpecificationRelation;
import com.gstuer.modelmerging.instance.pcm.surrogate.relation.SignatureProvisionRelation;

public class RepositoryTransformerTest extends TransformerTest<RepositoryTransformer, PcmSurrogate, Repository> {
    @Test
    public void testTransformSingleComponent() {
        // Test data
        RepositoryTransformer transformer = createTransformer();
        PcmSurrogate model = createEmptyModel();
        Component component = Component.getUniquePlaceholder();

        model.add(component);

        // Execution
        Repository repository = transformer.transform(model);

        // Assertion
        assertNotNull(repository);
        assertTrue(containsRepresentative(repository, component));
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
        Component provider = Component.getUniquePlaceholder();
        Interface providerInterface = Interface.getUniquePlaceholder();
        InterfaceProvisionRelation interfaceProvision = new InterfaceProvisionRelation(provider,
                providerInterface, isPlaceholderRelation);

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
        Component consumer = Component.getUniquePlaceholder();
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
        Component provider = Component.getUniquePlaceholder();
        Interface providerInterface = Interface.getUniquePlaceholder();
        InterfaceProvisionRelation interfaceProvision = new InterfaceProvisionRelation(provider,
                providerInterface, false);
        Signature signature = Signature.getUniquePlaceholder();
        SignatureProvisionRelation signatureProvision = new SignatureProvisionRelation(signature,
                providerInterface, isPlaceholderRelation);

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
        Component provider = Component.getUniquePlaceholder();
        Interface providerInterface = Interface.getUniquePlaceholder();
        InterfaceProvisionRelation interfaceProvision = new InterfaceProvisionRelation(provider,
                providerInterface, false);
        Signature signature = Signature.getUniquePlaceholder();
        SignatureProvisionRelation signatureProvision = new SignatureProvisionRelation(signature,
                providerInterface, false);
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
