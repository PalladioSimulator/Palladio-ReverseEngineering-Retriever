package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.Collection;
import java.util.Objects;

/**
 * Used to build {@code Component}s.
 * 
 * @see Component
 * @author Florian Bossert
 */
public class ComponentBuilder {
    private final CompUnitOrName compUnitOrName;
    private final RequirementsBuilder requirements;
    private final ProvisionsBuilder provisions;

    public ComponentBuilder(CompUnitOrName compUnitOrName) {
        this.compUnitOrName = compUnitOrName;
        this.requirements = new RequirementsBuilder();
        this.provisions = new ProvisionsBuilder();
    }

    public RequirementsBuilder requirements() {
        return requirements;
    }

    public ProvisionsBuilder provisions() {
        return provisions;
    }

    public Component create(Collection<OperationInterface> allDependencies) {
        return new Component(compUnitOrName, requirements.create(), provisions.create(allDependencies));
    }

    @Override
    public int hashCode() {
        return Objects.hash(compUnitOrName, provisions, requirements);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ComponentBuilder other = (ComponentBuilder) obj;
        return Objects.equals(compUnitOrName, other.compUnitOrName) && Objects.equals(provisions, other.provisions)
                && Objects.equals(requirements, other.requirements);
    }
}
