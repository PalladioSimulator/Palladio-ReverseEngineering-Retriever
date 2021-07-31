package org.palladiosimulator.somox.analyzer.rules.engine.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.emftext.language.java.parameters.impl.OrdinaryParameterImpl;
import org.emftext.language.java.parameters.impl.VariableLengthParameterImpl;
import org.emftext.language.java.types.impl.IntImpl;
import org.emftext.language.java.types.impl.ShortImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.palladiosimulator.pcm.repository.CollectionDataType;
import org.palladiosimulator.pcm.repository.Interface;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.repository.PrimitiveDataType;
import org.palladiosimulator.pcm.repository.PrimitiveTypeEnum;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;

public class BasicTest extends RuleEngineTest {

    protected BasicTest() {
        super("BasicProject", DefaultRule.JAX_RS);
    }

    /**
     * Tests the basic functionality of the RuleEngineAnalyzer.
     * Requires it to execute without an exception and produce an output file.
     */
    @Test
    void test() {
        assertEquals(2, getComponents().size());
        assertEquals(1, getDatatypes().size());
        assertEquals(0, getFailuretypes().size());
        assertEquals(2, getInterfaces().size());
        
        assertTrue(OUT_DIR.resolve("pcm.repository").toFile().exists());
    }

    @Test
    @Disabled("This bug is inherited from Palladio, this can only be fixed after it is fixed there.")
    void testShort() {
        OperationInterface conflictingMethods = getConflictingMethods();
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
    
    @Test
    void testArray() {
        OperationInterface conflictingMethods = getConflictingMethods();
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
    
    @Test
    void testVararg() {
        OperationInterface conflictingMethods = getConflictingMethods();
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
    
    private OperationInterface getConflictingMethods() {
        OperationInterface conflictingMethods = null;
        for (Interface iface : getInterfaces()) {
            if (iface.getEntityName().equals("basic_ConflictingMethods")) {
                assertTrue(iface instanceof OperationInterface);
                conflictingMethods = (OperationInterface) iface;
            }
        }
        assertNotNull(conflictingMethods);
        return conflictingMethods;
    }
}
