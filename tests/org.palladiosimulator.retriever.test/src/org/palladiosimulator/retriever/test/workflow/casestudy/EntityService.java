package org.palladiosimulator.retriever.test.workflow.casestudy;

public class EntityService {
    private final EntityRepository entityRepository;

    public EntityService(EntityRepository entityRepository) {
        this.entityRepository = entityRepository;
    }

    public Entity get(String identifier) {
        return this.entityRepository.findByIdentifier(identifier);
    }

    public Entity save(Entity entity) {
        return this.entityRepository.persist(entity);
    }

    public void delete(Entity entity) {
        this.entityRepository.remove(entity);
    }
}
