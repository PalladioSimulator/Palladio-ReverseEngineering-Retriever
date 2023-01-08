package org.palladiosimulator.somox.analyzer.rules.mocore.surrogate;

import tools.mdsd.mocore.framework.surrogate.ModelTest;
import tools.mdsd.mocore.framework.surrogate.Relation;
import tools.mdsd.mocore.utility.SimpleElement;
import tools.mdsd.mocore.utility.SimpleRelation;

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
