package org.palladiosimulator.somox.analyzer.rules.mocore.surrogate;

import org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.PcmSurrogate;

import com.gstuer.modelmerging.framework.surrogate.ModelTest;
import com.gstuer.modelmerging.framework.surrogate.Relation;
import com.gstuer.modelmerging.utility.SimpleElement;
import com.gstuer.modelmerging.utility.SimpleRelation;

public class PcmSurrogateTest extends ModelTest<PcmSurrogate, SimpleElement> {
    @Override
    protected PcmSurrogate createEmptyModel() {
        return new PcmSurrogate();
    }

    @Override
    protected SimpleElement createUniqueReplaceable() {
        return new SimpleElement(false);
    }

    @Override
    protected Relation<SimpleElement, SimpleElement> createRelation(SimpleElement source, SimpleElement destination,
            boolean isPlaceholder) {
        return new SimpleRelation(source, destination, isPlaceholder);
    }
}
