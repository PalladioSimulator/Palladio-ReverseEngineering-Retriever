package org.palladiosimulator.retriever.test.workflow.casestudy;

public class EntityRepository {
    private static int identifierCounter = Integer.MIN_VALUE;

    public Entity findByIdentifier(final String identifier) {
        return new Entity(identifier);
    }

    public Entity persist(final Entity entity) {
        if (entity.getIdentifier() == null) {
            return new Entity(String.valueOf(identifierCounter++));
        } else {
            return entity;
        }
    }

    public boolean remove(final Entity entity) {
        return true;
    }
}
