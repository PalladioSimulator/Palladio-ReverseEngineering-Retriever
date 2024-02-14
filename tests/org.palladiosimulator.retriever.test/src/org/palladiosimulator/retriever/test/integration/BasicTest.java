package org.palladiosimulator.retriever.test.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.palladiosimulator.pcm.repository.CollectionDataType;
import org.palladiosimulator.pcm.repository.Interface;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.repository.PrimitiveDataType;
import org.palladiosimulator.pcm.repository.PrimitiveTypeEnum;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.retriever.core.main.RetrieverException;
import org.palladiosimulator.retriever.core.workflow.RetrieverJob;
import org.palladiosimulator.retriever.extraction.blackboard.RetrieverBlackboard;
import org.palladiosimulator.retriever.extraction.engine.RetrieverConfiguration;
import org.palladiosimulator.retriever.extraction.engine.Rule;
import org.palladiosimulator.retriever.extraction.rules.JaxRSRules;

import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class BasicTest extends CaseStudyTest {

    private static final String PROJECT_NAME = "BasicProject";
    private static final Rule[] RULES = { new JaxRSRules() };

    protected BasicTest() {
        super(PROJECT_NAME, RULES);
        this.loadArtifacts();
    }

    private OperationInterface getConflictingMethods(final List<Interface> interfaces) {
        OperationInterface conflictingMethods = null;
        for (final Interface iface : interfaces) {
            if ("basic_ConflictingMethods".equals(iface.getEntityName())) {
                assertTrue(iface instanceof OperationInterface);
                conflictingMethods = (OperationInterface) iface;
            }
        }
        assertNotNull(conflictingMethods);
        return conflictingMethods;
    }

    @Disabled("FIXME: Reliance on outdated JaxRS rule")
    @Test
    void testArray() {
        final OperationInterface conflictingMethods = this.getConflictingMethods(this.getInterfaces());
        for (final OperationSignature sig : conflictingMethods.getSignatures__OperationInterface()) {
            for (final Parameter param : sig.getParameters__OperationSignature()) {
                if ("intArray".equals(param.getParameterName())) {
                    assertTrue(param.getDataType__Parameter() instanceof CollectionDataType);
                    final CollectionDataType collDT = (CollectionDataType) param.getDataType__Parameter();
                    assertTrue(collDT.getInnerType_CollectionDataType() instanceof PrimitiveDataType);
                    final PrimitiveDataType primDT = (PrimitiveDataType) collDT.getInnerType_CollectionDataType();
                    assertEquals(PrimitiveTypeEnum.INT, primDT.getType());
                }
            }
        }
    }

    /**
     * Retriever produced inconsistent results if executed multiple times. Arguments and methods
     * appear multiple times. This probably has something to do with (discouraged) static states
     * somewhere in the stack.
     *
     * @throws ModelAnalyzerException
     *             forwarded from Retriever. Should cause the test to fail.
     * @throws UserCanceledException
     *             should not happen since no user is in the loop.
     * @throws JobFailedException
     *             forwarded from JavaDiscoverer. Should cause the test to fail.
     */
    @Disabled("FIXME: Reliance on outdated JaxRS rule")
    @Test
    void testRepeatability() throws RetrieverException, JobFailedException, UserCanceledException {
        OperationInterface conflictingMethods = this.getConflictingMethods(this.getInterfaces());
        int firstIntArgCount = 0;
        for (final OperationSignature sig : conflictingMethods.getSignatures__OperationInterface()) {
            for (final Parameter param : sig.getParameters__OperationSignature()) {
                if ("intArg".equals(param.getParameterName())) {
                    firstIntArgCount++;
                }
            }
        }

        // Run Retriever again on the same project
        final RetrieverConfiguration retrieverConfig = this.getConfig();
        retrieverConfig.setOutputFolder(retrieverConfig.getOutputFolder()
            .appendSegment("repeated"));
        final RetrieverJob retrieverJob = new RetrieverJob(retrieverConfig);
        retrieverJob.execute(new NullProgressMonitor());
        final Repository repo = (Repository) retrieverJob.getBlackboard()
            .getPartition(RetrieverBlackboard.KEY_REPOSITORY);

        conflictingMethods = this.getConflictingMethods(repo.getInterfaces__Repository());

        int secondIntArgCount = 0;
        for (final OperationSignature sig : conflictingMethods.getSignatures__OperationInterface()) {
            for (final Parameter param : sig.getParameters__OperationSignature()) {
                if ("intArg".equals(param.getParameterName())) {
                    secondIntArgCount++;
                }
            }
        }

        assertEquals(firstIntArgCount, secondIntArgCount);
    }

    @Disabled("This bug is inherited from Palladio, this can only be fixed after it is fixed there.")
    @Test
    void testShort() {
        final OperationInterface conflictingMethods = this.getConflictingMethods(this.getInterfaces());
        for (final OperationSignature sig : conflictingMethods.getSignatures__OperationInterface()) {
            for (final Parameter param : sig.getParameters__OperationSignature()) {
                if ("shortArg".equals(param.getParameterName())) {
                    assertTrue(param.getDataType__Parameter() instanceof PrimitiveDataType);
                    final PrimitiveDataType primDT = (PrimitiveDataType) param.getDataType__Parameter();
                    assertNotEquals(PrimitiveTypeEnum.INT, primDT.getType());
                }
            }
        }
    }

    @Disabled("FIXME: Reliance on outdated JaxRS rule")
    @Test
    void testVararg() {
        final OperationInterface conflictingMethods = this.getConflictingMethods(this.getInterfaces());
        for (final OperationSignature sig : conflictingMethods.getSignatures__OperationInterface()) {
            for (final Parameter param : sig.getParameters__OperationSignature()) {
                if ("longVararg".equals(param.getParameterName())) {
                    assertTrue(param.getDataType__Parameter() instanceof CollectionDataType);
                    final CollectionDataType collDT = (CollectionDataType) param.getDataType__Parameter();
                    assertTrue(collDT.getInnerType_CollectionDataType() instanceof PrimitiveDataType);
                    final PrimitiveDataType primDT = (PrimitiveDataType) collDT.getInnerType_CollectionDataType();
                    assertEquals(PrimitiveTypeEnum.LONG, primDT.getType());
                }
            }
        }
    }
}
