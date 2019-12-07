package com.eoscode.springapitools.data.repository;

import com.eoscode.springapitools.service.exceptions.EntityNotFoundException;
import org.springframework.data.repository.NoRepositoryBean;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
            return Optional.empty();
        }

        Map<String, Object> hints = new HashMap<>();
        hints.put("javax.persistence.fetchgraph", graph);

        Entity entity = entityManger.find(entityClass, id, hints);

        if (entity == null) {
            throw new EntityNotFoundException(
                    "Object not found! Id: " + id + ", Type: " + entityClass.getName());
        }

        return Optional.of(entity);

    }

}
