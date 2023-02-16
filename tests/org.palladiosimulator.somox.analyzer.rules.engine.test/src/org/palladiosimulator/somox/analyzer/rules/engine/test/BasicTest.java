package org.palladiosimulator.somox.analyzer.rules.engine.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.palladiosimulator.pcm.repository.CollectionDataType;
import org.palladiosimulator.pcm.repository.Interface;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.repository.PrimitiveDataType;
import org.palladiosimulator.pcm.repository.PrimitiveTypeEnum;
import org.palladiosimulator.pcm.repository.impl.RepositoryImpl;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.somox.analyzer.rules.main.RuleEngineAnalyzer;
import org.palladiosimulator.somox.analyzer.rules.main.RuleEngineException;
import org.palladiosimulator.somox.discoverer.Discoverer;
import org.palladiosimulator.somox.discoverer.EmfTextDiscoverer;
import org.palladiosimulator.somox.discoverer.JavaDiscoverer;

import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class BasicTest extends RuleEngineTest {

	private static final String PROJECT_NAME = "BasicProject";
	private static final DefaultRule[] RULES = { DefaultRule.JAX_RS, DefaultRule.JAX_RS_EMFTEXT };

	protected BasicTest() {
		super(PROJECT_NAME, RULES);
	}

	private OperationInterface getConflictingMethods(List<Interface> interfaces) {
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

	/**
	 * Tests the basic functionality of the RuleEngineAnalyzer. Requires it to
	 * execute without an exception and produce an output file.
	 */
	@Override
	@Test
	void test() {
		assertTrue(new File(getOutputDirectory().appendSegment("pcm.repository").devicePath()).exists());
	}

	@Test
	void testArray() {
		final OperationInterface conflictingMethods = getConflictingMethods(getInterfaces());
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
	 * The RuleEngine produced inconsistent results if executed multiple times.
	 * Arguments and methods appear multiple times. This probably has something to
	 * do with (discouraged) static states somewhere in the stack.
	 *
	 * @throws ModelAnalyzerException forwarded from RuleEngineAnalyzer. Should
	 *                                cause the test to fail.
	 * @throws UserCanceledException  should not happen since no user is in the
	 *                                loop.
	 * @throws JobFailedException     forwarded from JavaDiscoverer. Should cause
	 *                                the test to fail.
	 */
	@Test
	void testRepeatability(boolean emfText) throws RuleEngineException, JobFailedException, UserCanceledException {
		OperationInterface conflictingMethods = getConflictingMethods(getInterfaces());
		int firstIntArgCount = 0;
		for (final OperationSignature sig : conflictingMethods.getSignatures__OperationInterface()) {
			for (final Parameter param : sig.getParameters__OperationSignature()) {
				if ("intArg".equals(param.getParameterName())) {
					firstIntArgCount++;
				}
			}
		}

		// Run the RuleEngine again on the same project
		final RuleEngineBlackboard blackboard = new RuleEngineBlackboard();
		final RuleEngineAnalyzer analyzer = new RuleEngineAnalyzer(blackboard);
		final Discoverer discoverer = emfText ? new EmfTextDiscoverer() : new JavaDiscoverer();
		discoverer.create(getConfig(), blackboard).execute(null);

		analyzer.analyze(getConfig(), null);

		final String discovererSegment = emfText ? "emfText" : "jdt";
		final RepositoryImpl repo = loadRepository(
				OUT_DIR.appendSegment(discovererSegment).appendSegment("pcm.repository"));
		conflictingMethods = getConflictingMethods(repo.getInterfaces__Repository());

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
		final OperationInterface conflictingMethods = getConflictingMethods(getInterfaces());
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

	@Test
	void testVararg() {
		final OperationInterface conflictingMethods = getConflictingMethods(getInterfaces());
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
