package org.palladiosimulator.somox.analyzer.rules.engine.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.emftext.language.java.containers.impl.CompilationUnitImpl;
import org.junit.jupiter.api.Disabled;
import org.palladiosimulator.pcm.repository.CollectionDataType;
import org.palladiosimulator.pcm.repository.Interface;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.repository.PrimitiveDataType;
import org.palladiosimulator.pcm.repository.PrimitiveTypeEnum;
import org.palladiosimulator.pcm.repository.impl.RepositoryImpl;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;
import org.palladiosimulator.somox.analyzer.rules.engine.ParserAdapter;
import org.palladiosimulator.somox.analyzer.rules.main.RuleEngineAnalyzer;

public class BasicTest extends RuleEngineTest {
    
    private static final String PROJECT_NAME = "BasicProject";
    private static final DefaultRule[] RULES = {DefaultRule.JAX_RS};

    protected BasicTest() {
        super(PROJECT_NAME, RULES);
    }

    /**
     * Tests the basic functionality of the RuleEngineAnalyzer.
     * Requires it to execute without an exception and produce an output file.
     */
    @Disabled("Disabled due to build server using OpenJDK")
    void test() {
        // if this fails, the STL might have changed... these numbers are for JDK 11.0.2
        // Disabled due to build server using OpenJDK
        /*
        assertEquals(284, getComponents().size());
        assertEquals(310, getDatatypes().size());
        assertEquals(0, getFailuretypes().size());
        assertEquals(137, getInterfaces().size());
        */
        
        assertTrue(OUT_DIR.resolve("pcm.repository").toFile().exists());
    }

    @Disabled("This bug is inherited from Palladio, this can only be fixed after it is fixed there.")
    void testShort() {
        OperationInterface conflictingMethods = getConflictingMethods(getInterfaces());
        for (OperationSignature sig : conflictingMethods.getSignatures__OperationInterface()) {
            for (Parameter param : sig.getParameters__OperationSignature()) {
                if (param.getParameterName().equals("shortArg")) {
                    assertTrue(param.getDataType__Parameter() instanceof PrimitiveDataType);
                    PrimitiveDataType primDT = (PrimitiveDataType) param.getDataType__Parameter();
                    assertNotEquals(PrimitiveTypeEnum.INT, primDT.getType());
                }
            }
        }
    }
    
    @Disabled("Disabled due to build server using OpenJDK")
    void testArray() {
        OperationInterface conflictingMethods = getConflictingMethods(getInterfaces());
        for (OperationSignature sig : conflictingMethods.getSignatures__OperationInterface()) {
            for (Parameter param : sig.getParameters__OperationSignature()) {
                if (param.getParameterName().equals("intArray")) {
                    assertTrue(param.getDataType__Parameter() instanceof CollectionDataType);
                    CollectionDataType collDT = (CollectionDataType) param.getDataType__Parameter();
                    assertTrue(collDT.getInnerType_CollectionDataType() instanceof PrimitiveDataType);
                    PrimitiveDataType primDT = (PrimitiveDataType) collDT.getInnerType_CollectionDataType();
                    assertEquals(PrimitiveTypeEnum.INT, primDT.getType());
                }
            }
        }
    }
    
    @Disabled("Disabled due to build server using OpenJDK")
    void testVararg() {
        OperationInterface conflictingMethods = getConflictingMethods(getInterfaces());
        for (OperationSignature sig : conflictingMethods.getSignatures__OperationInterface()) {
            for (Parameter param : sig.getParameters__OperationSignature()) {
                if (param.getParameterName().equals("longVararg")) {
                    assertTrue(param.getDataType__Parameter() instanceof CollectionDataType);
                    CollectionDataType collDT = (CollectionDataType) param.getDataType__Parameter();
                    assertTrue(collDT.getInnerType_CollectionDataType() instanceof PrimitiveDataType);
                    PrimitiveDataType primDT = (PrimitiveDataType) collDT.getInnerType_CollectionDataType();
                    assertEquals(PrimitiveTypeEnum.LONG, primDT.getType());
                }
            }
        }
    }
    
    /**
     * The RuleEngine produces inconsistent results if executed multiple times.
     * Arguments and methods appear multiple times. This probably has something to do
     * with (discouraged) static states somewhere in the stack.
     */
    @Disabled("Disabled due to build server using OpenJDK")
    void testRepeatability() {
        OperationInterface conflictingMethods = getConflictingMethods(getInterfaces());
        int firstIntArgCount = 0;
        for (OperationSignature sig : conflictingMethods.getSignatures__OperationInterface()) {
            for (Parameter param : sig.getParameters__OperationSignature()) {
                if (param.getParameterName().equals("intArg")) {
                    firstIntArgCount++;
                }
            }
        }

        // Run the RuleEngine again on the same project
        final Path inPath = TEST_DIR.resolve(PROJECT_NAME);
        final List<CompilationUnitImpl> model = ParserAdapter.generateModelForPath(inPath, OUT_DIR);
        RuleEngineAnalyzer.executeWith(inPath, OUT_DIR, model, getRules());
        Path repoPath = OUT_DIR.resolve("pcm.repository");
        RepositoryImpl repo = loadRepository(URI.createFileURI(repoPath.toString()));
        conflictingMethods = getConflictingMethods(repo.getInterfaces__Repository());

        int secondIntArgCount = 0;
        for (OperationSignature sig : conflictingMethods.getSignatures__OperationInterface()) {
            for (Parameter param : sig.getParameters__OperationSignature()) {
                if (param.getParameterName().equals("intArg")) {
                    secondIntArgCount++;
                }
            }
        }

        assertEquals(firstIntArgCount, secondIntArgCount);
    }
    
    private OperationInterface getConflictingMethods(List<Interface> interfaces) {
        OperationInterface conflictingMethods = null;
        for (Interface iface : interfaces) {
            if (iface.getEntityName().equals("basic_ConflictingMethods")) {
                assertTrue(iface instanceof OperationInterface);
                conflictingMethods = (OperationInterface) iface;
            }
        }
        assertNotNull(conflictingMethods);
        return conflictingMethods;
    }
}
