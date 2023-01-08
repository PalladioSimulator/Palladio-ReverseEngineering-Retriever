package org.palladiosimulator.somox.analyzer.rules.mocore.transformation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.palladiosimulator.somox.analyzer.rules.mocore.utility.PcmEvaluationUtility.containsRepresentative;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Component;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Interface;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.ServiceEffectSpecification;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element.Signature;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.ComponentSignatureProvisionRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.InterfaceProvisionRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.InterfaceRequirementRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.ServiceEffectSpecificationRelation;
import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.relation.SignatureProvisionRelation;

import tools.mdsd.mocore.framework.transformation.TransformerTest;

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
