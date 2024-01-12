package org.palladiosimulator.retriever.mocore.surrogate.relation;

import org.palladiosimulator.retriever.mocore.surrogate.element.Composite;

import tools.mdsd.mocore.framework.surrogate.Relation;
import tools.mdsd.mocore.framework.surrogate.Replaceable;

public class CompositeProvisionDelegationRelation
        extends Relation<InterfaceProvisionRelation, InterfaceProvisionRelation> {
    private static final String ERROR_NOT_COMPOSITE = "Composite must be delegating (= source) part of relation.";
    private static final String ERROR_SAME_RELATION = "Delegations may not exist between equal source and destination.";
    private static final String ERROR_NON_EQUAL_INTERFACES = "Interfaces of the given relations must be equal.";

    public CompositeProvisionDelegationRelation(InterfaceProvisionRelation source,
            InterfaceProvisionRelation destination, boolean isPlaceholder) {
        super(source, destination, isPlaceholder);

        // Check whether relations are equal
        if (source.equals(destination)) {
            throw new IllegalArgumentException(ERROR_SAME_RELATION);
        }

        // Check whether the delegating component is a composite
        if (!Composite.class.isAssignableFrom(source.getSource()
            .getClass())) {
            throw new IllegalArgumentException(ERROR_NOT_COMPOSITE);
        }

        // Check whether interfaces are equal
        if (!source.getDestination()
            .equals(destination.getDestination())) {
            // TODO Allow child/parent interfaces
            throw new IllegalArgumentException(ERROR_NON_EQUAL_INTERFACES);
        }
    }

    @Override
    public <T extends Replaceable> CompositeProvisionDelegationRelation replace(T original, T replacement) {
        if (!this.includes(original)) {
            // TODO Add message to exception
            throw new IllegalArgumentException();
        }
        if (this.equals(original)) {
            return (CompositeProvisionDelegationRelation) replacement;
        }
        InterfaceProvisionRelation source = getSourceReplacement(original, replacement);
        InterfaceProvisionRelation destination = getDestinationReplacement(original, replacement);
        return new CompositeProvisionDelegationRelation(source, destination, this.isPlaceholder());
    }
}
