package com.gstuer.modelmerging.instance.pcm.orchestration;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.gstuer.modelmerging.instance.pcm.surrogate.PcmSurrogate;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Component;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Deployment;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Interface;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.LinkResourceSpecification;
import com.gstuer.modelmerging.instance.pcm.surrogate.element.Signature;
import com.gstuer.modelmerging.instance.pcm.surrogate.relation.ComponentAllocationRelation;
import com.gstuer.modelmerging.instance.pcm.surrogate.relation.ComponentAssemblyRelation;
import com.gstuer.modelmerging.instance.pcm.surrogate.relation.DeploymentDeploymentRelation;
import com.gstuer.modelmerging.instance.pcm.surrogate.relation.InterfaceProvisionRelation;
import com.gstuer.modelmerging.instance.pcm.surrogate.relation.InterfaceRequirementRelation;
import com.gstuer.modelmerging.instance.pcm.surrogate.relation.LinkResourceSpecificationRelation;
import com.gstuer.modelmerging.instance.pcm.surrogate.relation.SignatureProvisionRelation;
import com.gstuer.modelmerging.instance.pcm.utility.ElementFactory;

public class PcmOrchestratorTest {
    @Test
    public void testExistsComponentAfterProcess() {
        PcmOrchestrator orchestrator = new PcmOrchestrator();
        Component component = ElementFactory.createUniqueComponent(false);
        orchestrator.processDiscovery(component);
        assertTrue(orchestrator.getModel().contains(component));
    }

    @Test
    public void testExistImplicitReplaceablesAfterProcess() {
        PcmOrchestrator orchestrator = new PcmOrchestrator();
        Component component = ElementFactory.createUniqueComponent(false);
        orchestrator.processDiscovery(component);

        PcmSurrogate model = orchestrator.getModel();
        List<Deployment> deployments = model.getByType(Deployment.class);
        Stream<ComponentAllocationRelation> componentDeploymentRelations = model
                .getByType(ComponentAllocationRelation.class).stream();

        // Assertions
        assertTrue(model.contains(component));
        assertTrue(componentDeploymentRelations.anyMatch(element -> element.getSource().equals(component)));
        assertFalse(deployments.isEmpty());
    }

    @Test
    public void testExistsDeploymentAfterProcess() {
        PcmOrchestrator orchestrator = new PcmOrchestrator();
        Deployment deployment = ElementFactory.createUniqueDeployment(false);
        orchestrator.processDiscovery(deployment);
        assertTrue(orchestrator.getModel().contains(deployment));
    }

    @Test
    public void testChainReplacementOfPlaceholders() {
        // Test data
        PcmOrchestrator orchestrator = new PcmOrchestrator();

        //// Create concrete & placeholder elements
        Signature concreteSignature = ElementFactory.createUniqueSignature(false);
        Interface concreteInterface = ElementFactory.createUniqueInterface(false);
        Component concreteComponent = ElementFactory.createUniqueComponent(false);
        Deployment concreteDeployment = ElementFactory.createUniqueDeployment(false);
        Interface placeholderInterface = Interface.getUniquePlaceholder();
        Component placeholderComponent = Component.getUniquePlaceholder();
        Deployment placeholderDeployment = Deployment.getUniquePlaceholder();

        //// Create non-conflicting relations between elements
        SignatureProvisionRelation placeholderSignatureProvision = new SignatureProvisionRelation(concreteSignature,
                placeholderInterface, true);
        InterfaceProvisionRelation placeholderInterfaceProvision = new InterfaceProvisionRelation(placeholderComponent,
                placeholderInterface, true);
        ComponentAllocationRelation placeholderAllocation = new ComponentAllocationRelation(placeholderComponent,
                placeholderDeployment, true);
        InterfaceProvisionRelation concreteInterfaceProvision = new InterfaceProvisionRelation(concreteComponent,
                concreteInterface, false);
        ComponentAllocationRelation concreteAllocation = new ComponentAllocationRelation(concreteComponent,
                concreteDeployment, false);

        //// Create relation leading to chain replacement
        SignatureProvisionRelation signatureProvisionRelation = new SignatureProvisionRelation(concreteSignature,
                concreteInterface, false);

        //// Add entities to model
        PcmSurrogate model = orchestrator.getModel();
        model.add(concreteSignature);
        model.add(concreteInterface);
        model.add(concreteComponent);
        model.add(concreteDeployment);
        model.add(concreteInterfaceProvision);
        model.add(concreteAllocation);
        model.add(placeholderInterface);
        model.add(placeholderComponent);
        model.add(placeholderDeployment);
        model.add(placeholderSignatureProvision);
        model.add(placeholderInterfaceProvision);
        model.add(placeholderAllocation);

        // Execution
        orchestrator.processDiscovery(signatureProvisionRelation);

        // Assertions: Post-execution
        assertTrue(model.contains(concreteSignature));
        assertTrue(model.contains(concreteInterface));
        assertTrue(model.contains(concreteComponent));
        assertTrue(model.contains(concreteDeployment));
        assertTrue(model.contains(signatureProvisionRelation));
        assertTrue(model.contains(concreteInterfaceProvision));
        assertTrue(model.contains(concreteAllocation));

        assertFalse(model.contains(placeholderInterface));
        assertFalse(model.contains(placeholderComponent));
        assertFalse(model.contains(placeholderDeployment));
        assertFalse(model.contains(placeholderSignatureProvision));
        assertFalse(model.contains(placeholderInterfaceProvision));
        assertFalse(model.contains(placeholderAllocation));

        assertEquals(1, model.getByType(Signature.class).size());
        assertEquals(1, model.getByType(Interface.class).size());
        assertEquals(1, model.getByType(Component.class).size());
        assertEquals(1, model.getByType(Deployment.class).size());
        assertEquals(0, model.getByType(LinkResourceSpecification.class).size());

        assertEquals(1, model.getByType(SignatureProvisionRelation.class).size());
        assertEquals(1, model.getByType(InterfaceProvisionRelation.class).size());
        assertEquals(0, model.getByType(InterfaceRequirementRelation.class).size());
        assertEquals(1, model.getByType(ComponentAllocationRelation.class).size());
        assertEquals(0, model.getByType(ComponentAssemblyRelation.class).size());
        assertEquals(0, model.getByType(DeploymentDeploymentRelation.class).size());
        assertEquals(0, model.getByType(LinkResourceSpecificationRelation.class).size());
    }
}
