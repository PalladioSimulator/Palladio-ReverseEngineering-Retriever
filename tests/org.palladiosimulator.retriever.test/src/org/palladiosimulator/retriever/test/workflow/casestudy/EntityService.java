package org.palladiosimulator.retriever.test.workflow.casestudy;

public class EntityService {
    private final EntityRepository entityRepository;

    public EntityService(final EntityRepository entityRepository) {
        this.entityRepository = entityRepository;
    }

    public Entity get(final String identifier) {
        return this.entityRepository.findByIdentifier(identifier);
    }

    public Entity save(final Entity entity) {
        return this.entityRepository.persist(entity);
    }

    public void delete(final Entity entity) {
        this.entityRepository.remove(entity);
    }
}
