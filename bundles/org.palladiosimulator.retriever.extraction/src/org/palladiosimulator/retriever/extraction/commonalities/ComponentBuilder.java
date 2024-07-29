package org.palladiosimulator.retriever.extraction.commonalities;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

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
    private Optional<String> separatingIdentifier;

    public ComponentBuilder(final CompUnitOrName compUnitOrName) {
        this.compUnitOrName = compUnitOrName;
        this.requirements = new RequirementsBuilder();
        this.provisions = new ProvisionsBuilder();
        this.separatingIdentifier = Optional.empty();
    }

    public CompUnitOrName identifier() {
        return this.compUnitOrName;
    }

    public RequirementsBuilder requirements() {
        return this.requirements;
    }

    public ProvisionsBuilder provisions() {
        return this.provisions;
    }

    public void setSeparatingIdentifier(final String separatingIdentifier) {
        this.separatingIdentifier = Optional.of(separatingIdentifier);
    }

    public Component create(final Collection<OperationInterface> allDependencies,
            final Collection<OperationInterface> visibleProvisions) {
        return new Component(this.compUnitOrName, this.requirements.create(allDependencies, visibleProvisions),
                this.provisions.create(allDependencies), this.separatingIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.compUnitOrName, this.provisions, this.requirements);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (this.getClass() != obj.getClass())) {
            return false;
        }
        final ComponentBuilder other = (ComponentBuilder) obj;
        return Objects.equals(this.compUnitOrName, other.compUnitOrName)
                && Objects.equals(this.provisions, other.provisions)
                && Objects.equals(this.requirements, other.requirements);
    }
}
