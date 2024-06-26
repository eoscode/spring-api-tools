package com.eoscode.springapitools.data.repository;

import com.eoscode.springapitools.exceptions.EntityNotFoundException;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@NoRepositoryBean
public class BaseRepository<Entity, ID> {

    @PersistenceContext
    private EntityManager entityManger;

    EntityManager getEntityManger() {
        return entityManger;
    }

    Optional<Entity> findWithEntityGraph(String entityGraph, Class<Entity> entityClass, ID id) {

        EntityGraph<?> graph = entityManger.createEntityGraph(entityClass.getSimpleName()+"."+entityGraph);
        if (graph == null) {
            return Optional.of(entityManger.find(entityClass, id));
        }

        Map<String, Object> hints = new HashMap<>();
        hints.put("jakarta.persistence.fetchgraph", graph);

        Entity entity = entityManger.find(entityClass, id, hints);

        if (entity == null) {
            throw new EntityNotFoundException(
                    "Object not found! Id: " + id + ", Type: " + entityClass.getName());
        }

        return Optional.of(entity);

    }

}
