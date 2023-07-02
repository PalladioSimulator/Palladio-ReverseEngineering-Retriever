package org.palladiosimulator.somox.analyzer.rules.workflow.casestudy;

public class EntityRepository {
    private static int identifierCounter = Integer.MIN_VALUE;

    public Entity findByIdentifier(String identifier) {
        return new Entity(identifier);
    }

    public Entity persist(Entity entity) {
        if (entity.getIdentifier() == null) {
            return new Entity(String.valueOf(identifierCounter++));
        } else {
            return entity;
        }
    }

    public boolean remove(Entity entity) {
        return true;
    }
}
