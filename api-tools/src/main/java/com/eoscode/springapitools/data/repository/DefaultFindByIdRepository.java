package com.eoscode.springapitools.data.repository;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class DefaultFindByIdRepository<Entity, ID> extends BaseRepository<Entity, ID>
        implements CustomFindByIdRepository<Entity, ID> {

    @Override
    public Optional<Entity> findById(Class<Entity> entityClass, ID id) {
        return findWithEntityGraph("findById", entityClass, id);
    }

}
