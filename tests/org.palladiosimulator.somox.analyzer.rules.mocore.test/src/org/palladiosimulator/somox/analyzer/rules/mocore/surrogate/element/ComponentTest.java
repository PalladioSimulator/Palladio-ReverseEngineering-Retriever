package org.palladiosimulator.somox.analyzer.rules.mocore.surrogate.element;

import org.palladiosimulator.generator.fluent.repository.factory.FluentRepositoryFactory;
import org.palladiosimulator.pcm.repository.BasicComponent;

import tools.mdsd.mocore.framework.surrogate.ElementTest;
import tools.mdsd.mocore.utility.IdentifierGenerator;

public class ComponentTest extends ElementTest<Component, BasicComponent> {
    @Override
    protected Component createElement(BasicComponent value, boolean isPlaceholder) {
        return new Component(value, isPlaceholder);
    }

    @Override
    protected BasicComponent getUniqueValue() {
        String identifier = IdentifierGenerator.getUniqueIdentifier();
        BasicComponent value = new FluentRepositoryFactory().newBasicComponent().withName(identifier).build();
        return value;
    }

    @Override
    protected Component getUniqueNonPlaceholder() {
        return new Component(getUniqueValue(), false);
    }

    @Override
    protected Component getPlaceholderOf(Component replaceable) {
        return new Component(replaceable.getValue(), true);
    }
}
