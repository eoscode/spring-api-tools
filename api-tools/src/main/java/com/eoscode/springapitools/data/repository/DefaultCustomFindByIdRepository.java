package com.eoscode.springapitools.data.repository;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class DefaultCustomFindByIdRepository<Entity, ID> extends BaseRepository<Entity, ID>
        implements CustomFindByIdRepository<Entity, ID> {

    @Override
    public Optional<Entity> findCustomById(Class<Entity> entityClass, ID id) {
        return findWithEntityGraph("findById", entityClass, id);
    }

    @Override
    public Optional<Entity> findDetailById(Class<Entity> entityClass, ID id) {
        return findWithEntityGraph("findDetailById", entityClass, id);
    }


}
