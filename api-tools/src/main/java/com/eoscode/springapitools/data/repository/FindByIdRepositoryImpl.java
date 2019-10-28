package com.eoscode.springapitools.data.repository;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class FindByIdRepositoryImpl<Entity, ID> extends BaseCustomRepository implements FindByIdRepositoryCustom<Entity, ID> {

    @Override
    public Optional<Entity> findById(Class<Entity> entityClass, ID id) {
        return findWithEntityGraph("findById", entityClass, id);
    }

    @Override
    public Optional<Entity> findDetailById(Class<Entity> entityClass, ID id) {
        return findWithEntityGraph("detail", entityClass, id);
    }


}
